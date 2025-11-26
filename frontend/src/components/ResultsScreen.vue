<template>
  <div class="results-container">
    <h1>Resultados da Partida</h1>

    <!-- Estatísticas Individuais -->
    <h2>Estatísticas Individuais</h2>
    <div v-for="player in players" :key="player.name" class="player-stats">
      <h3>{{ player.name }}</h3>
      <table border="1">
        <thead>
          <tr>
            <th>Ação</th>
            <th>Tipo</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="(actions, actionName) in player.stats">
            <tr v-for="(count, type) in actions">
              <td>{{ actionName }}</td>
              <td>{{ type }}</td>
              <td>{{ count }}</td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <!-- Estatísticas Gerais da Equipe -->
    <h2>Estatísticas Gerais da Equipe</h2>
    <table border="1">
       <thead>
          <tr>
            <th>Ação</th>
            <th>Tipo</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="(actions, actionName) in teamStats">
            <tr v-for="(count, type) in actions">
              <td>{{ actionName }}</td>
              <td>{{ type }}</td>
              <td>{{ count }}</td>
            </tr>
          </template>
        </tbody>
    </table>

    <!-- Gráficos -->
    <h2>Gráficos de Desempenho</h2>
    <div v-for="player in players" :key="player.name + '-charts'" class="player-charts">
      <h3>{{ player.name }}</h3>
      <div v-for="(actions, actionName) in player.stats" :key="actionName" class="chart-container">
        <h4>{{ actionName }}</h4>
        <Pie :data="getChartData(actions)" />
      </div>
    </div>

    <button class="export-btn" @click="exportPDF">Exportar PDF</button>
    <button class="back-btn" @click="$emit('reset')">Nova Partida</button>
  </div>
</template>

<script>
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale } from 'chart.js'
import { jsPDF } from 'jspdf'
import 'jspdf-autotable'

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale)

export default {
  components: { Pie },
  props: ['players'],
  computed: {
    teamStats() {
      const stats = {};
      this.players.forEach(player => {
        for (const actionName in player.stats) {
          if (!stats[actionName]) {
            stats[actionName] = {};
          }
          for (const type in player.stats[actionName]) {
            if (!stats[actionName][type]) {
              stats[actionName][type] = 0;
            }
            stats[actionName][type] += player.stats[actionName][type];
          }
        }
      });
      return stats;
    }
  },
  methods: {
    getChartData(actions) {
      const labels = Object.keys(actions);
      const data = Object.values(actions);
      const backgroundColors = labels.map(() => `hsl(${Math.random() * 360}, 70%, 50%)`);

      return {
        labels,
        datasets: [{
          data,
          backgroundColor: backgroundColors,
        }]
      };
    },
    exportPDF() {
      const doc = new jsPDF();
      doc.text('Relatório da Partida', 14, 16);

      let startY = 30;

      // Estatísticas Individuais
      this.players.forEach(player => {
        doc.text(player.name, 14, startY);
        const tableData = [];
        for (const actionName in player.stats) {
          for (const type in player.stats[actionName]) {
            tableData.push([actionName, type, player.stats[actionName][type]]);
          }
        }
        doc.autoTable({
          startY: startY + 5,
          head: [['Ação', 'Tipo', 'Total']],
          body: tableData,
        });
        startY = doc.autoTable.previous.finalY + 15;
      });

      // Estatísticas da Equipe
      doc.text('Estatísticas Gerais da Equipe', 14, startY);
      const teamTableData = [];
      for (const actionName in this.teamStats) {
        for (const type in this.teamStats[actionName]) {
          teamTableData.push([actionName, type, this.teamStats[actionName][type]]);
        }
      }
      doc.autoTable({
        startY: startY + 5,
        head: [['Ação', 'Tipo', 'Total']],
        body: teamTableData,
      });

      doc.save('relatorio_partida.pdf');
    }
  }
};
</script>

<style scoped>
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

@media (max-width: 600px) {
  .player-stats, .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
