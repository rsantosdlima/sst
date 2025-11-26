<template>
  <div class="scout-container">
    <div class="header">
      <h2>{{ teamName }} - Set {{ currentSetIndex + 1 }}</h2>
      <p>{{ matchDate }}</p>
    </div>
    <div class="actions-grid">
      <div v-for="action in actions" :key="action.name" class="action-group">
        <h3>{{ action.name }}</h3>
        <button
          v-for="type in action.types"
          :key="type"
          @click="openPlayerModal(action.name, type)">
          {{ type }}
        </button>
      </div>
    </div>
    <button class="finish-btn" @click="$emit('finishScout')">Finalizar Set</button>

    <!-- Player Selection Modal -->
    <div v-if="showPlayerModal" class="modal-overlay" @click.self="closePlayerModal">
      <div class="modal-content">
        <h3>Selecione o Jogador</h3>
        <div class="players-grid">
          <button v-for="player in players" :key="player.name" @click="assignAction(player)">
            {{ player.name }}
          </button>
        </div>
        <button class="close-btn" @click="closePlayerModal">Cancelar</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: ['players', 'teamName', 'matchDate', 'currentSetIndex'],
  data() {
    return {
      showPlayerModal: false,
      selectedAction: {
        name: null,
        type: null
      },
      actions: [
        { name: 'Passe', types: ['A', 'B', 'C', 'ERRO'] },
        { name: 'Saque', types: ['Bom', 'Erro'] },
        { name: 'Ataque', types: ['Bom', 'Erro'] },
        { name: 'Defesa', types: ['A', 'B', 'Erro'] },
        { name: 'Bloqueio', types: ['Bom', 'Erro'] }
      ]
    };
  },
  methods: {
    openPlayerModal(name, type) {
      this.selectedAction = { name, type };
      this.showPlayerModal = true;
    },
    closePlayerModal() {
      this.showPlayerModal = false;
    },
    assignAction(player) {
      this.$emit('action', { player, action: this.selectedAction });
      this.closePlayerModal();
    }
  }
};
</script>

<style scoped>
.scout-container {
  padding: 15px;
}
.header {
  margin-bottom: 20px;
}
.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}
.action-group {
  background-color: #fff;
  padding: 10px;
  border-radius: 8px;
}
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.6);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}
.players-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;
}
.finish-btn, .close-btn {
  width: 100%;
  padding: 12px;
  margin-top: 20px;
}
</style>
