<template>
  <div class="results-container">
    <div class="header">
      <h1>Resultados do Set {{ currentSetIndex + 1 }}</h1>
      <h3>{{ teamName }} - {{ matchDate }}</h3>
    </div>

    <!-- Métricas de Eficiência -->
    <h2>Métricas de Eficiência</h2>
    <table border="1">
      <thead>
        <tr>
          <th>Jogador</th>
          <th>Aprov. Ataque (%)</th>
          <th>Efic. Passe (%)</th>
          <th>Aprov. Saque (%)</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="player in players" :key="player.name">
          <td>{{ player.name }}</td>
          <td>{{ calculateAttackEfficiency(player.stats) }}</td>
          <td>{{ calculatePassEfficiency(player.stats) }}</td>
          <td>{{ calculateServeEfficiency(player.stats) }}</td>
        </tr>
      </tbody>
    </table>

    <!-- Gráficos de Barras -->
    <h2>Desempenho por Ação</h2>
    <div class="charts-grid">
      <div v-for="action in Object.keys(teamStats)" :key="action" class="chart-container">
        <h3>{{ action }}</h3>
        <Bar :data="getBarChartData(action)" />
      </div>
    </div>

    <div class="actions">
      <button @click="$emit('nextSet')">Iniciar Próximo Set</button>
      <button @click="exportPDF">Exportar PDF do Set</button>
      <button @click="$emit('reset')">Finalizar e Nova Partida</button>
    </div>
  </div>
</template>

<script>
import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js'
import { jsPDF } from 'jspdf'
import 'jspdf-autotable'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

export default {
  components: { Bar },
  props: ['teamName', 'matchDate', 'sets', 'currentSetIndex'],
  computed: {
    players() {
      return this.sets[this.currentSetIndex]?.players || [];
    },
    teamStats() {
      // Team stats logic for the current set
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
    calculateAttackEfficiency(stats) {
      const good = stats['Ataque']?.['Bom'] || 0;
      const error = stats['Ataque']?.['Erro'] || 0;
      const total = good + error;
      return total > 0 ? (((good - error) / total) * 100).toFixed(2) : 0;
    },
    calculatePassEfficiency(stats) {
      const a = stats['Passe']?.['A'] || 0;
      const b = stats['Passe']?.['B'] || 0;
      const c = stats['Passe']?.['C'] || 0;
      const error = stats['Passe']?.['ERRO'] || 0;
      const total = a + b + c + error;
      return total > 0 ? (((a * 100 + b * 50) / total)).toFixed(2) : 0;
    },
    calculateServeEfficiency(stats) {
      const good = stats['Saque']?.['Bom'] || 0;
      const error = stats['Saque']?.['Erro'] || 0;
      const total = good + error;
      return total > 0 ? (((good - error) / total) * 100).toFixed(2) : 0;
    },
    getBarChartData(actionName) {
      const labels = this.players.map(p => p.name);
      const datasets = [];
      const actionTypes = Object.keys(this.teamStats[actionName] || {});

      actionTypes.forEach(type => {
        datasets.push({
          label: type,
          data: this.players.map(p => p.stats[actionName]?.[type] || 0),
          backgroundColor: `hsl(${Math.random() * 360}, 70%, 50%)`,
        });
      });

      return { labels, datasets };
    },
    exportPDF() {
      const doc = new jsPDF();
      doc.text(`${this.teamName} - Set ${this.currentSetIndex + 1}`, 14, 16);
      doc.text(`Data: ${this.matchDate}`, 14, 24);

      // Efficiency Metrics Table
      const efficiencyData = this.players.map(p => [
        p.name,
        this.calculateAttackEfficiency(p.stats),
        this.calculatePassEfficiency(p.stats),
        this.calculateServeEfficiency(p.stats)
      ]);

      doc.autoTable({
        startY: 30,
        head: [['Jogador', 'Aprov. Ataque (%)', 'Efic. Passe (%)', 'Aprov. Saque (%)']],
        body: efficiencyData,
      });

      // Raw Stats Table
      const rawData = [];
      this.players.forEach(p => {
        rawData.push([p.name, '', '', '']); // Player row
        for(const action in p.stats) {
          for(const type in p.stats[action]) {
            rawData.push(['', action, type, p.stats[action][type]]);
          }
        }
      });

      doc.autoTable({
        startY: doc.autoTable.previous.finalY + 10,
        head: [['Jogador', 'Ação', 'Tipo', 'Total']],
        body: rawData,
      });

      doc.save(`scout_${this.teamName}_set${this.currentSetIndex + 1}.pdf`);
    }
  }
};
</script>

<style scoped>
.results-container {
  padding: 20px;
}
table {
  width: 100%;
  margin-bottom: 30px;
}
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}
.actions {
  margin-top: 30px;
  display: flex;
  gap: 10px;
}
</style>
