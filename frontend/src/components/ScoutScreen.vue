<template>
  <div class="scout-container">
    <h2>Scout Ao Vivo</h2>
    <div class="actions-panel">
      <h3>Ações</h3>
      <div v-for="action in actions" :key="action.name">
        <h4>{{ action.name }}</h4>
        <button
          v-for="type in action.types"
          :key="type"
          @click="selectAction(action.name, type)"
          :class="{ selected: selectedAction.name === action.name && selectedAction.type === type }">
          {{ type }}
        </button>
      </div>
    </div>
    <div class="players-panel">
      <h3>Jogadores</h3>
      <div v-for="(player, index) in players" :key="index">
        <button @click="assignAction(player)" :disabled="!selectedAction.name">
          {{ player.name }}
        </button>
      </div>
    </div>
    <button class="finish-btn" @click="$emit('finishScout')">Finalizar Scout</button>
  </div>
</template>

<script>
export default {
  props: ['players'],
  data() {
    return {
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
    selectAction(name, type) {
      this.selectedAction = { name, type };
    },
    assignAction(player) {
      if (this.selectedAction.name) {
        this.$emit('action', { player, action: this.selectedAction });
        this.selectedAction = { name: null, type: null };
      }
    }
  }
};
</script>

<style scoped>
.scout-container {
  display: grid;
  grid-template-areas:
    "actions players"
    "finish finish";
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.actions-panel {
  grid-area: actions;
}

.players-panel {
  grid-area: players;
}

.finish-btn {
  grid-area: finish;
}

.actions-grid, .players-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 10px;
}

@media (max-width: 600px) {
  .scout-container {
    grid-template-areas:
      "actions"
      "players"
      "finish";
    grid-template-columns: 1fr;
  }
}
</style>
