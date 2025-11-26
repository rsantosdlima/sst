<template>
  <div class="results-container">
    <div class="header">
      <h1>Dashboard do Jogo</h1>
      <h3>{{ teamName }} - {{ matchDate }}</h3>
      <div class="scores-summary">
        <h4>Placares:</h4>
        <span v-for="(set, index) in sets" :key="index">
          Set {{ index + 1 }}: {{ set.score.ourScore }} x {{ set.score.opponentScore }}
        </span>
      </div>
    </div>

    <div class="filter-container">
      <label for="set-filter">Visualizar Estatísticas:</label>
      <select id="set-filter" v-model="selectedView">
        <option value="all">Geral (Todos os Sets)</option>
        <option v-for="(set, index) in sets" :key="index" :value="index">
          Set {{ index + 1 }}
        </option>
      </select>
    </div>

    <div v-for="fundament in fundaments" :key="fundament.name">
      <h2>{{ fundament.name }}</h2>
      <div class="stats-grid">
        <div class="table-container">
          <table border="1">
            <thead>
              <tr>
                <th>Jogador</th>
                <th>Acertos</th>
                <th>Erros</th>
                <th>Total</th>
                <th>Erros (%)</th>
                <th>Aproveitamento (%)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="player in detailedStats" :key="player.name">
                <td>{{ player.name }}</td>
                <td>{{ player[fundament.key].good }}</td>
                <td>{{ player[fundament.key].error }}</td>
                <td>{{ player[fundament.key].total }}</td>
                <td>{{ player[fundament.key].errorPercentage }}</td>
                <td>{{ player[fundament.key].efficiency }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="chart-container" :id="`chart-${fundament.key}`">
          <BarChart v-if="hasDataForChart(fundament.key)" :chart-data="getChartData(fundament.key)" />
        </div>
      </div>
    </div>

    <div class="actions">
      <button @click="$emit('nextSet')" v-if="sets.length < 5">Iniciar Próximo Set</button>
      <button @click="exportPDF">Exportar PDF</button>
      <button @click="$emit('reset')">Finalizar e Nova Partida</button>
    </div>
  </div>
</template>

<script>
import { jsPDF } from 'jspdf'
import 'jspdf-autotable'
import BarChart from './BarChart.vue'

export default {
  components: { BarChart },
  props: ['teamName', 'matchDate', 'sets'],
  data() {
    return {
      selectedView: 'all',
      fundaments: [
        { name: 'Ataque', key: 'attack' }, { name: 'Passe', key: 'pass' },
        { name: 'Saque', key: 'serve' }, { name: 'Defesa', key: 'defense' },
        { name: 'Bloqueio', key: 'block' },
      ]
    }
  },
  computed: {
    detailedStats() {
      const players = this.sets[0]?.players.map(p => ({ name: p.name, stats: {} })) || [];

      const setsToProcess = this.selectedView === 'all'
        ? this.sets
        : [this.sets[this.selectedView]];

      players.forEach(player => {
        setsToProcess.forEach(set => {
          const setPlayer = set.players.find(p => p.name === player.name);
          if (setPlayer) {
            for (const action in setPlayer.stats) {
              if (!player.stats[action]) player.stats[action] = {};
              for (const type in setPlayer.stats[action]) {
                if (!player.stats[action][type]) player.stats[action][type] = 0;
                player.stats[action][type] += setPlayer.stats[action][type];
              }
            }
          }
        });
      });

      return players.map(player => {
        const stats = player.stats;
        const attackGood = stats['Ataque']?.['Bom'] || 0;
        const attackError = stats['Ataque']?.['Erro'] || 0;
        const attackTotal = attackGood + attackError;

        const passA = stats['Passe']?.['A'] || 0;
        const passB = stats['Passe']?.['B'] || 0;
        const passC = stats['Passe']?.['C'] || 0;
        const passError = stats['Passe']?.['ERRO'] || 0;
        const passTotal = passA + passB + passC + passError;

        const serveGood = stats['Saque']?.['Bom'] || 0;
        const serveError = stats['Saque']?.['Erro'] || 0;
        const serveTotal = serveGood + serveError;

        const defenseGood = (stats['Defesa']?.['A'] || 0) + (stats['Defesa']?.['B'] || 0);
        const defenseError = stats['Defesa']?.['Erro'] || 0;
        const defenseTotal = defenseGood + defenseError;

        const blockGood = stats['Bloqueio']?.['Bom'] || 0;
        const blockError = stats['Bloqueio']?.['Erro'] || 0;
        const blockTotal = blockGood + blockError;

        return {
          name: player.name,
          attack: { good: attackGood, error: attackError, total: attackTotal, errorPercentage: attackTotal > 0 ? ((attackError / attackTotal) * 100).toFixed(2) : 0, efficiency: attackTotal > 0 ? (((attackGood - attackError) / attackTotal) * 100).toFixed(2) : 0, },
          pass: { good: passA + passB + passC, error: passError, total: passTotal, errorPercentage: passTotal > 0 ? ((passError / passTotal) * 100).toFixed(2) : 0, efficiency: passTotal > 0 ? (((passA * 100 + passB * 50 + passC * 25) / passTotal)).toFixed(2) : 0, },
          serve: { good: serveGood, error: serveError, total: serveTotal, errorPercentage: serveTotal > 0 ? ((serveError / serveTotal) * 100).toFixed(2) : 0, efficiency: serveTotal > 0 ? (((serveGood - serveError) / serveTotal) * 100).toFixed(2) : 0, },
          defense: { good: defenseGood, error: defenseError, total: defenseTotal, errorPercentage: defenseTotal > 0 ? ((defenseError / defenseTotal) * 100).toFixed(2) : 0, efficiency: defenseTotal > 0 ? (((defenseGood - defenseError) / defenseTotal) * 100).toFixed(2) : 0, },
          block: { good: blockGood, error: blockError, total: blockTotal, errorPercentage: blockTotal > 0 ? ((blockError / blockTotal) * 100).toFixed(2) : 0, efficiency: blockTotal > 0 ? (((blockGood - blockError) / blockTotal) * 100).toFixed(2) : 0, }
        };
      });
    },
    attackChartData() { return this.createChartData('attack'); },
    passChartData() { return this.createChartData('pass'); },
    serveChartData() { return this.createChartData('serve'); },
    defenseChartData() { return this.createChartData('defense'); },
    blockChartData() { return this.createChartData('block'); }
  },
  methods: {
    hasDataForChart(fundamentKey) {
        return this.detailedStats.some(player => player[fundamentKey].total > 0);
    },
    createChartData(fundamentKey) {
      const labels = this.detailedStats.map(p => p.name);
      return {
        labels,
        datasets: [
          {
            label: 'Acertos',
            backgroundColor: '#4CAF50',
            data: this.detailedStats.map(p => p[fundamentKey].good)
          },
          {
            label: 'Erros',
            backgroundColor: '#F44336',
            data: this.detailedStats.map(p => p[fundamentKey].error)
          }
        ]
      };
    },
    getChartData(fundamentKey) {
        switch(fundamentKey) {
            case 'attack': return this.attackChartData;
            case 'pass': return this.passChartData;
            case 'serve': return this.serveChartData;
            case 'defense': return this.defenseChartData;
            case 'block': return this.blockChartData;
            default: return {};
        }
    },
    async exportPDF() {
      const doc = new jsPDF();
      const viewTitle = this.selectedView === 'all' ? 'Geral (Todos os Sets)' : `Set ${this.selectedView + 1}`;
      doc.text(`${this.teamName} - ${viewTitle}`, 14, 16);
      doc.text(`Data: ${this.matchDate}`, 14, 24);
      let startY = 30;

      for (const fundament of this.fundaments) {
          if (startY > 250) { // Add new page if content is too long
              doc.addPage();
              startY = 20;
          }

          doc.text(fundament.name, 14, startY);

          const tableData = this.detailedStats.map(player => [
              player.name, player[fundament.key].good, player[fundament.key].error,
              player[fundament.key].total, player[fundament.key].errorPercentage, player[fundament.key].efficiency,
          ]);

          doc.autoTable({
              startY: startY + 5,
              head: [['Jogador', 'Acertos', 'Erros', 'Total', 'Erros (%)', 'Aproveitamento (%)']],
              body: tableData,
              theme: 'striped',
              headStyles: { fillColor: [22, 160, 133] },
          });

          let finalY = doc.autoTable.previous.finalY;

          // Add chart
          const chartElement = document.querySelector(`#chart-${fundament.key} canvas`);
          if (chartElement && this.hasDataForChart(fundament.key)) {
              const chartImage = chartElement.toDataURL('image/png');
              const chartHeight = 50; // A fixed height for the chart in the PDF
              const chartWidth = 90;

              if (finalY + chartHeight + 15 > doc.internal.pageSize.height) {
                  doc.addPage();
                  finalY = 15;
              }
              doc.addImage(chartImage, 'PNG', 14, finalY + 10, chartWidth, chartHeight);
              startY = finalY + chartHeight + 20;
          } else {
              startY = finalY + 15;
          }
      }

      const pdfBlob = doc.output('blob');
      const fileName = `scout_${this.teamName}_${this.selectedView === 'all' ? 'geral' : 'set' + (this.selectedView + 1)}.pdf`;

      if (navigator.share) {
        try {
          await navigator.share({
            files: [new File([pdfBlob], fileName, { type: 'application/pdf' })],
            title: `Relatório - ${viewTitle}`,
          });
        } catch (error) {
          console.error('Erro ao compartilhar:', error);
          const link = document.createElement('a');
          link.href = URL.createObjectURL(pdfBlob);
          link.download = fileName;
          link.click();
        }
      } else {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(pdfBlob);
        link.download = fileName;
        link.click();
      }
    }
  }
};
</script>

<style scoped>
.results-container { padding: 20px; }
.scores-summary { margin: 15px 0; }
.scores-summary span { margin-right: 15px; }
.filter-container { margin-bottom: 20px; }
.stats-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  align-items: center;
  margin-bottom: 30px;
}
.table-container { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { border: 1px solid #ddd; padding: 8px; }
.chart-container { height: 250px; }
.actions { margin-top: 30px; display: flex; gap: 10px; }
</style>
