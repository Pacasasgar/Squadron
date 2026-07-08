const API_BASE = '/api/games';
let currentGameId = null;

// UI Elements
const startOverlay = document.getElementById('start-overlay');
const gameUi = document.getElementById('game-ui');
const btnNewGame = document.getElementById('btn-new-game');
const combatLog = document.getElementById('combat-log');
const defensesContainer = document.getElementById('defenses-container');

// Stat Elements
const elWave = document.getElementById('val-wave');
const elHealth = document.getElementById('val-health');
const elGold = document.getElementById('val-gold');
const elIncome = document.getElementById('val-income');

// Initialize
btnNewGame.addEventListener('click', createNewGame);

async function createNewGame() {
    try {
        const response = await fetch(API_BASE, { method: 'POST' });
        const game = await response.json();
        
        currentGameId = game.id;
        
        // Hide overlay, show UI
        startOverlay.classList.add('hidden');
        gameUi.classList.remove('hidden');
        
        updateHUD(game);
        appendLog(`> Partida #${currentGameId} iniciada. Prepárate para la primera oleada.`);
    } catch (error) {
        console.error("Error creando partida", error);
        alert("Error al conectar con el servidor.");
    }
}

async function buyDefense(type) {
    if (!currentGameId) return;
    try {
        const response = await fetch(`${API_BASE}/${currentGameId}/defenses?type=${type}`, { method: 'POST' });
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || "No tienes oro suficiente.");
        }
        const game = await response.json();
        updateHUD(game);
        appendLog(`> Comprada defensa: ${type}`);
    } catch (error) {
        appendLog(`> <span class="damage">[ERROR]</span> ${error.message}`);
    }
}

async function investEconomy() {
    if (!currentGameId) return;
    try {
        const response = await fetch(`${API_BASE}/${currentGameId}/economy`, { method: 'POST' });
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || "No tienes oro suficiente para mejorar la economía.");
        }
        const game = await response.json();
        updateHUD(game);
        appendLog(`> Economía mejorada. Nuevo income: +${game.income}`);
    } catch (error) {
        appendLog(`> <span class="damage">[ERROR]</span> ${error.message}`);
    }
}

async function startWave() {
    if (!currentGameId) return;
    try {
        const response = await fetch(`${API_BASE}/${currentGameId}/start-wave`, { method: 'POST' });
        if (!response.ok) throw new Error("Error resolviendo la oleada.");
        
        const logText = await response.text();
        
        // Formatear el log para que quede bonito
        const formattedLog = logText
            .replace(/--- INICIO DE OLEADA (.*) ---/g, '<span class="wave">--- INICIO DE OLEADA $1 ---</span>')
            .replace(/Recompensa(.*?)\+/g, '<span class="reward">Recompensa$1+</span>')
            .replace(/daño/gi, '<span class="damage">daño</span>')
            .replace(/GAME OVER/g, '<span class="damage">GAME OVER</span>')
            .split('\n')
            .filter(line => line.trim().length > 0)
            .map(line => `> ${line}`)
            .join('<br>');
            
        appendLog(formattedLog);
        
        // Update game state
        refreshGameState();
    } catch (error) {
        appendLog(`> <span class="damage">[ERROR]</span> ${error.message}`);
    }
}

async function refreshGameState() {
    if (!currentGameId) return;
    const response = await fetch(`${API_BASE}/${currentGameId}`);
    const game = await response.json();
    updateHUD(game);
    
    if (game.status === 'GAME_OVER') {
        if (!document.getElementById('btn-start-wave').disabled) {
            appendLog(`> <span class="damage">Has perdido la partida. Refresca la página para volver a intentar.</span>`);
            disableUI();
        }
    } else if (game.status === 'VICTORY') {
        if (!document.getElementById('btn-start-wave').disabled) {
            appendLog(`> <span class="reward">¡HAS GANADO! ¡Sobreviviste a todas las oleadas!</span>`);
            disableUI();
        }
    }
}

function disableUI() {
    document.getElementById('btn-start-wave').disabled = true;
    document.querySelectorAll('.btn-buy').forEach(btn => btn.disabled = true);
    document.querySelectorAll('.btn-buy').forEach(btn => btn.style.opacity = '0.5');
    document.getElementById('btn-start-wave').style.opacity = '0.5';
}

function updateHUD(game) {
    elWave.innerText = game.currentWave;
    elHealth.innerText = game.baseHealth;
    elGold.innerText = game.gold;
    elIncome.innerText = `+${game.income}`;
    
    // Cambiar color si la vida baja
    if (game.baseHealth <= 30) {
        elHealth.style.color = 'var(--danger)';
    } else {
        elHealth.style.color = 'white';
    }

    renderDefenses(game.defenses);
}

function renderDefenses(defenses) {
    defensesContainer.innerHTML = '';
    
    const iconMap = {
        'INFANTRY': '🛡️',
        'ARCHER': '🏹',
        'KNIGHT': '⚔️'
    };

    if (defenses.length === 0) {
        defensesContainer.innerHTML = '<p style="color: var(--text-muted); grid-column: 1/-1; text-align: center;">No hay defensas colocadas.</p>';
        return;
    }

    defenses.forEach(def => {
        const div = document.createElement('div');
        div.className = 'defense-card';
        div.innerHTML = `
            <div class="icon">${iconMap[def.type] || '♟️'}</div>
            <div class="type">${def.type}</div>
        `;
        defensesContainer.appendChild(div);
    });
}

function appendLog(htmlContent) {
    const p = document.createElement('p');
    p.innerHTML = htmlContent;
    combatLog.appendChild(p);
    // Scroll to bottom
    combatLog.scrollTop = combatLog.scrollHeight;
}
