<template>
  <div id="app" class="container">
    <div v-if="!matchStarted && !showResults">
      <h1>Configuração da Partida</h1>
      <div class="match-info-group">
        <input v-model="teamName" placeholder="Nome do Time">
        <input type="date" v-model="matchDate">
      </div>
      <div class="player-input-group">
        <input v-model="newPlayer" @keyup.enter="addPlayer" placeholder="Nome ou número do jogador">
        <button @click="addPlayer">Adicionar Jogador</button>
      </div>
      <h2>Jogadores:</h2>
      <ul>
        <li v-for="(player, index) in players" :key="index">
          {{ player.name }}
          <button class="remove-btn" @click="removePlayer(index)">X</button>
        </li>
      </ul>
      <button class="start-btn" @click="startMatch" :disabled="players.length === 0">Iniciar Partida</button>
    </div>
    <ScoutScreen
      v-else-if="matchStarted && !showResults"
      :players="players"
      :teamName="teamName"
      :matchDate="matchDate"
      :currentSetIndex="currentSetIndex"
      @action="recordAction"
      @finishScout="showResultsScreen"
    />
    <ResultsScreen
      v-else-if="showResults"
      :teamName="teamName"
      :matchDate="matchDate"
      :sets="sets"
      :currentSetIndex="currentSetIndex"
      @nextSet="startNextSet"
      @reset="resetApp"
    />
  </div>
</template>

<script>
import ScoutScreen from './components/ScoutScreen.vue';
import ResultsScreen from './components/ResultsScreen.vue';

export default {
  components: {
    ScoutScreen,
    ResultsScreen
  },
  data() {
    return {
      teamName: '',
      matchDate: new Date().toISOString().slice(0, 10),
      newPlayer: '',
      players: [],
      sets: [],
      currentSetIndex: 0,
      matchStarted: false,
      showResults: false
    };
  },
  methods: {
    addPlayer() {
      if (this.newPlayer.trim() !== '') {
        this.players.push({ name: this.newPlayer.trim() });
        this.newPlayer = '';
      }
    },
    removePlayer(index) {
      this.players.splice(index, 1);
    },
    startMatch() {
      if (this.players.length > 0) {
        this.sets.push({ players: JSON.parse(JSON.stringify(this.players.map(p => ({...p, stats: {}})))) });
        this.matchStarted = true;
      }
    },
    recordAction(event) {
      const { player, action } = event;
      const set = this.sets[this.currentSetIndex];
      const playerRef = set.players.find(p => p.name === player.name);

      if (playerRef) {
        if (!playerRef.stats[action.name]) {
          playerRef.stats[action.name] = {};
        }
        if (!playerRef.stats[action.name][action.type]) {
          playerRef.stats[action.name][action.type] = 0;
        }
        playerRef.stats[action.name][action.type]++;
      }
    },
    showResultsScreen(score) {
      this.sets[this.currentSetIndex].score = score;
      this.matchStarted = false;
      this.showResults = true;
    },
    startNextSet() {
      this.currentSetIndex++;
      this.sets.push({ players: JSON.parse(JSON.stringify(this.players.map(p => ({...p, stats: {}})))) });
      this.showResults = false;
      this.matchStarted = true;
    },
    resetApp() {
      this.teamName = '';
      this.matchDate = new Date().toISOString().slice(0, 10);
      this.players = [];
      this.sets = [];
      this.currentSetIndex = 0;
      this.showResults = false;
    }
  }
};
</script>

<style>
* {
  box-sizing: border-box;
}

.container {
  max-width: 800px;
  margin: 20px auto;
  padding: 15px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f4f7f6;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

@media (max-width: 600px) {
  .container {
    margin: 10px;
    padding: 10px;
  }

  .player-input-group {
    flex-direction: column;
  }

  input, button {
    width: 100%;
    margin-left: 0;
    margin-bottom: 10px;
  }
}

.player-input-group {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

input {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  width: 60%;
}

button {
  padding: 10px 15px;
  border: none;
  background-color: #007bff;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 10px;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.remove-btn {
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 25px;
  height: 25px;
  cursor: pointer;
}

.start-btn {
  width: 100%;
  padding: 15px;
  font-size: 1.2em;
  background-color: #28a745;
}
</style>
