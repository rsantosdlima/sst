<template>
  <div class="results-container">
    <div class="header">
      <h1>Resultados do Set {{ currentSetIndex + 1 }}</h1>
      <h3>{{ teamName }} - {{ matchDate }}</h3>
    </div>

    <div v-for="fundament in fundaments" :key="fundament.name">
      <h2>{{ fundament.name }}</h2>
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

    <div class="actions">
      <button @click="$emit('nextSet')">Iniciar Próximo Set</button>
      <button @click="exportPDF">Exportar PDF do Set</button>
      <button @click="$emit('reset')">Finalizar e Nova Partida</button>
    </div>
  </div>
</template>

<script>
import { jsPDF } from 'jspdf'
import 'jspdf-autotable'

export default {
  props: ['teamName', 'matchDate', 'sets', 'currentSetIndex'],
  data() {
    return {
      fundaments: [
        { name: 'Ataque', key: 'attack' },
        { name: 'Passe', key: 'pass' },
        { name: 'Saque', key: 'serve' },
        { name: 'Defesa', key: 'defense' },
        { name: 'Bloqueio', key: 'block' },
      ]
    }
  },
  computed: {
    players() {
      return this.sets[this.currentSetIndex]?.players || [];
    },
    detailedStats() {
      return this.players.map(player => {
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
          attack: {
            good: attackGood,
            error: attackError,
            total: attackTotal,
            errorPercentage: attackTotal > 0 ? ((attackError / attackTotal) * 100).toFixed(2) : 0,
            efficiency: attackTotal > 0 ? (((attackGood - attackError) / attackTotal) * 100).toFixed(2) : 0,
          },
          pass: {
            good: passA + passB + passC,
            error: passError,
            total: passTotal,
            errorPercentage: passTotal > 0 ? ((passError / passTotal) * 100).toFixed(2) : 0,
            efficiency: passTotal > 0 ? (((passA * 100 + passB * 50 + passC * 25) / passTotal)).toFixed(2) : 0,
          },
          serve: {
            good: serveGood,
            error: serveError,
            total: serveTotal,
            errorPercentage: serveTotal > 0 ? ((serveError / serveTotal) * 100).toFixed(2) : 0,
            efficiency: serveTotal > 0 ? (((serveGood - serveError) / serveTotal) * 100).toFixed(2) : 0,
          },
          defense: {
              good: defenseGood,
              error: defenseError,
              total: defenseTotal,
              errorPercentage: defenseTotal > 0 ? ((defenseError / defenseTotal) * 100).toFixed(2) : 0,
              efficiency: defenseTotal > 0 ? (((defenseGood - defenseError) / defenseTotal) * 100).toFixed(2) : 0,
          },
          block: {
              good: blockGood,
              error: blockError,
              total: blockTotal,
              errorPercentage: blockTotal > 0 ? ((blockError / blockTotal) * 100).toFixed(2) : 0,
              efficiency: blockTotal > 0 ? (((blockGood - blockError) / blockTotal) * 100).toFixed(2) : 0,
          }
        };
      });
    }
  },
  methods: {
    async exportPDF() {
      const doc = new jsPDF();
      doc.text(`${this.teamName} - Set ${this.currentSetIndex + 1}`, 14, 16);
      doc.text(`Data: ${this.matchDate}`, 14, 24);
      let startY = 30;

      this.fundaments.forEach(fundament => {
        doc.text(fundament.name, 14, startY);
        const tableData = this.detailedStats.map(player => [
          player.name,
          player[fundament.key].good,
          player[fundament.key].error,
          player[fundament.key].total,
          player[fundament.key].errorPercentage,
          player[fundament.key].efficiency,
        ]);

        doc.autoTable({
          startY: startY + 5,
          head: [['Jogador', 'Acertos', 'Erros', 'Total', 'Erros (%)', 'Aproveitamento (%)']],
          body: tableData,
        });

        startY = doc.autoTable.previous.finalY + 15;
      });

      const pdfBlob = doc.output('blob');
      const fileName = `scout_${this.teamName}_set${this.currentSetIndex + 1}.pdf`;

      if (navigator.share) {
        try {
          await navigator.share({
            files: [new File([pdfBlob], fileName, { type: 'application/pdf' })],
            title: `Relatório do Set ${this.currentSetIndex + 1}`,
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
.results-container {
  padding: 20px;
}
table {
  width: 100%;
  margin-bottom: 30px;
  border-collapse: collapse;
}
th, td {
  border: 1px solid #ddd;
  padding: 8px;
}
.actions {
  margin-top: 30px;
  display: flex;
  gap: 10px;
}
</style>
