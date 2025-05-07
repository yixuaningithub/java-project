<template>
  <div class="flex flex-col items-center justify-end h-screen bg-white relative overflow-hidden">
    <h1
      class="absolute top-4 left-1/2 transform -translate-x-1/2 
            text-[clamp(1.5rem,5vw,3rem)] font-extrabold 
            bg-gradient-to-r from-orange-400 via-yellow-400 to-red-500 
            text-transparent bg-clip-text animate-gradient"
    >
      Basketball Game
    </h1>
    <div id="hoop" class="absolute top-[8vh] left-1/2 transform -translate-x-1/2 w-[28vw] max-w-[140px] h-auto z-10">
      <svg viewBox="0 0 200 150" xmlns="http://www.w3.org/2000/svg">
        <rect x="20" y="10" width="160" height="100" rx="8" stroke="#888" fill="none" stroke-width="6"/>
        <rect x="70" y="30" width="60" height="40" stroke="#888" fill="none" stroke-width="4"/>
        <line x1="60" y1="70" x2="140" y2="70" stroke="#e53e3e" stroke-width="6"/>
        <rect x="85" y="72" width="30" height="8" fill="#555" rx="2"/>
      </svg>
    </div>

    <img
      v-if="!showBall"
      ref="staticBall"
      src="https://upload.wikimedia.org/wikipedia/commons/7/7a/Basketball.png"
      alt="ball"
      class="w-10 h-10 absolute z-10"
      :style="{
        left: staticBallX + 'px',
        bottom: staticBallY + 'px',
        transform: 'translate(-50%, 0)'
      }"
    />

    <img
      v-if="showBall"
      src="https://upload.wikimedia.org/wikipedia/commons/7/7a/Basketball.png"
      alt="ball"
      class="w-10 h-10 absolute z-10"
      :style="{
        left: ballX + 'px',
        bottom: ballY + 'px',
        transform: 'translate(-50%, 0)'
      }"
    />

    <div class="w-full max-w-md flex flex-col items-center mb-8 z-20">
      <div class="w-full h-6 bg-gray-200 rounded-full border border-gray-400 shadow-inner mb-2">
        <div
          class="h-full rounded-full transition-all duration-75"
          :style="{
            width: power + '%',
            background: isPerfect
              ? 'linear-gradient(to right, #f59e0b, #fcd34d)'
              : isBonus
              ? 'linear-gradient(to right, #ef4444, #f87171)'
              : powerSuccessHighlight
              ? 'linear-gradient(to right, #06b6d4, #3b82f6)'
              : 'linear-gradient(to right, #4ade80, #22c55e)'
          }"
        ></div>
      </div>
      <p v-if="isBonus" class="text-yellow-500 text-xl font-bold mt-2 animate-bounce">
        💥 BONUS +2!
      </p>
      <p v-if="isPerfect" class="text-yellow-500 text-xl font-bold mt-2 animate-bounce">
        🔥 PERFECT +5!
      </p>
      <div
        class="w-24 h-24 rounded-full bg-orange-500 text-white text-xl font-semibold 
              flex items-center justify-center 
              shadow-lg hover:shadow-xl 
              transition-transform duration-200 hover:scale-105 active:scale-95
              cursor-pointer select-none"
        @mousedown="startCharging"
        @mouseup="stopCharging"
        @mouseleave="stopCharging"
        @touchstart.prevent="startCharging"
        @touchend.prevent="stopCharging"
      >
        Shoot
      </div>

      <div v-if="!hasStarted" class="fixed inset-0 bg-white flex flex-col items-center justify-center z-50">
        <div class="typewriter-loop text-3xl font-bold text-gray-800 mb-6">
          Get Ready to Play!
        </div>
        <button
          @click="startGame"
          class="glow-on-hover px-6 py-3 bg-blue-600 text-white rounded-lg text-xl hover:bg-blue-700"
        >
          Start Game
        </button>
      </div>


      <p class="text-lg mt-4">Score: {{ score }}</p>
      <p class="text-lg">Remaining: {{ timeLeft }}s</p>
      <p v-if="timeLeft === 0" class="mt-4 text-xl font-bold">Time's up!🎉</p>
    </div>
  </div>

  <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white p-6 rounded-lg shadow-xl w-[90%] max-w-md text-center">
      <h2 class="text-2xl font-bold mb-4">Time's up!</h2>
      <p class="text-lg mb-2">Your score: {{ score }}</p>
      
      <input
        v-model="playerName"
        type="text"
        placeholder="Enter your name"
        class="w-full border p-2 rounded mb-4"
      />

      <button
        @click="submitScore"
        :disabled="hasSubmitted"
        class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 mb-4"
      >
        Submit
      </button>

      <div v-if="showLeaderboard" class="mb-4 max-h-60 overflow-y-auto border rounded p-2 shadow-inner bg-gray-50">
        <h3 class="text-2xl font-bold mb-4 bg-gradient-to-r from-pink-500 via-yellow-500 to-red-500 text-transparent bg-clip-text animate-gradient">
          🏆 Leaderboard
        </h3>
        <ul class="space-y-1 text-lg font-semibold text-gray-800">
          <li
            v-for="(entry, index) in leaderboard"
            :key="index"
            class="flex justify-between px-4 py-2 bg-white bg-opacity-90 rounded shadow hover:bg-yellow-100 transition"
          >
            <span class="font-extrabold text-xl text-indigo-600">
              #{{ index + 1 }}
            </span>
            <span class="ml-2">{{ entry.username }}</span>
            <span>{{ entry.score }}</span>
          </li>
        </ul>
      </div>

      <button
        @click="resetGame"
        class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
      >
        Play Again
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';

const power = ref(0);
const charging = ref(false);
const chargingDirection = ref(1);
const powerSuccessHighlight = ref(false);
const score = ref(0);
const timeLeft = ref(10);
const showBall = ref(false);
const isAnimating = ref(false);
const isSuccess = ref(false);
const isBonus = ref(false);
const isPerfect = ref(false);
const hasStarted = ref(false);
const hasSubmitted = ref(false);

const showModal = ref(false);
const playerName = ref('');
const leaderboard = ref([]);
const showLeaderboard = ref(false);

const staticBallX = ref(window.innerWidth / 2);
const staticBallY = ref(250);
const ballX = ref(window.innerWidth / 2);
const ballY = ref(0);

const API_URL = import.meta.env.VITE_API_URL

let powerInterval = null;
let countdownInterval = null;

watch(showModal, (val) => {
  if (val) loadLeaderboard();
});

const startGame = () => {
  hasStarted.value = true;
  timeLeft.value = 10;
  countdownInterval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
    } else {
      clearInterval(countdownInterval);
      clearInterval(powerInterval);
      showModal.value = true;
    }
  }, 1000);
};

let lastFrameTime = 0;
let animationFrameId = null;
const CHARGE_CYCLE_TIME = 2000;
const MAX_POWER = 100;

const animatePower = (timestamp) => {
  if (!charging.value) return;

  if (!lastFrameTime) lastFrameTime = timestamp;
  const elapsed = timestamp - lastFrameTime;
  lastFrameTime = timestamp;

  const delta = (MAX_POWER * 2 * elapsed) / CHARGE_CYCLE_TIME;
  power.value += chargingDirection.value * delta;

  if (power.value >= MAX_POWER) {
    power.value = MAX_POWER;
    chargingDirection.value = -1;
  } else if (power.value <= 0) {
    power.value = 0;
    chargingDirection.value = 1;
  }

  animationFrameId = requestAnimationFrame(animatePower);
};

const startCharging = () => {
  if (timeLeft.value === 0 || isAnimating.value) return;
  charging.value = true;
  chargingDirection.value = 1;
  lastFrameTime = 0;
  animationFrameId = requestAnimationFrame(animatePower);
};

const stopCharging = () => {
  if (!charging.value || timeLeft.value === 0) return;
  charging.value = false;
  cancelAnimationFrame(animationFrameId);
  chargingDirection.value = 1;
  isPerfect.value = power.value === 100;
  isBonus.value = power.value >= 95 && power.value < 100;
  powerSuccessHighlight.value = power.value >= 90;
  triggerBallAnimation();
};


const triggerBallAnimation = () => {
  isAnimating.value = true;
  showBall.value = true;

  const currentPower = power.value;
  isSuccess.value = currentPower >= 90;

  const startX = staticBallX.value;
  const startY = staticBallY.value;

  ballX.value = startX;
  ballY.value = startY;

  const hoop = document.getElementById('hoop');
  const hoopRect = hoop.getBoundingClientRect();

  const totalFrames = 60;
  const peakFrame = 30;
  const g = window.innerHeight / 3000;

  let targetX, targetY;

  if (isSuccess.value) {
    targetX = hoopRect.left + (100 / 200) * hoopRect.width;
    const targetY_px = hoopRect.top + (50 / 150) * hoopRect.height;
    targetY = window.innerHeight - targetY_px;
  } else {
    const isLeft = Math.random() < 0.5;
    const missX = isLeft ? 85 : 115;
    targetX = hoopRect.left + (missX / 200) * hoopRect.width;
    const targetY_px = hoopRect.top + (50 / 150) * hoopRect.height;
    targetY = window.innerHeight - targetY_px;
  }
  const peakY = targetY + window.innerHeight * 0.1;
  const vx = (targetX - startX) / peakFrame;
  const vy = (peakY - startY) / peakFrame + 0.5 * g * peakFrame;

  let frame = 0;

  const animate = () => {
    const t = frame;
    ballX.value = startX + vx * t;
    ballY.value = startY + vy * t - 0.5 * g * t ** 2;

    if (frame < totalFrames) {
      frame++;
      requestAnimationFrame(animate);
    } else {
      if (isSuccess.value) {
        if (isPerfect.value) score.value += 5;
        else if (isBonus.value) score.value += 2;
        else score.value += 1;
      }
      showBall.value = false;
      power.value = 0;
      isAnimating.value = false;
      powerSuccessHighlight.value = false;
      isBonus.value = false;
      isPerfect.value = false;
    }
  };

  requestAnimationFrame(animate);
};

const submitScore = async () => {
  if (!playerName.value || hasSubmitted.value) return;
  hasSubmitted.value = true;
  try {
    await fetch(API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: playerName.value,
        score: score.value,
      })
    });

    const res = await fetch(API_URL);
    const data = await res.json();
    leaderboard.value = data;
    showLeaderboard.value = true;

  } catch (err) {
    console.error("Failed to submit or fetch scores:", err);
    alert("Error submitting score. Please try again.");
  }
};


const resetGame = () => {
  clearInterval(countdownInterval);
  clearInterval(powerInterval);

  score.value = 0;
  timeLeft.value = 10;
  power.value = 0;
  charging.value = false;
  chargingDirection.value = 1;
  showModal.value = false;
  hasStarted.value = false;
  hasSubmitted.value = false;
};

const loadLeaderboard = async () => {
  try {
    const res = await fetch(`${API_URL}?t=${Date.now()}`);
    leaderboard.value = await res.json();
  } catch (err) {
    console.error("Failed to load leaderboard:", err);
  }
};

onMounted(() => {
  nextTick(() => {
    const btn = document.querySelector(".rounded-full.text-xl");
    if (btn) {
      const rect = btn.getBoundingClientRect();
      staticBallX.value = rect.left + rect.width / 2;
      staticBallY.value = window.innerHeight - rect.top + 30;
    }
  });
  loadLeaderboard();
});

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId);
  clearInterval(countdownInterval);

});
</script>

<style scoped>
@keyframes gradient-x {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.animate-gradient {
  background-size: 200% 200%;
  animation: gradient-x 4s ease infinite;
}

@keyframes typingLoop {
  0% {
    width: 0%;
  }
  50% {
    width: 50%;
  }
  100% {
    width: 0%;
  }
}

.typewriter-loop {
  overflow: hidden;
  border-right: 2px solid orange;
  white-space: nowrap;
  width: 0;
  animation: typingLoop 4s steps(30, end) infinite, blink-caret 0.75s step-end infinite;
}

@keyframes blink-caret {
  from, to { border-color: transparent }
  50% { border-color: orange }
}

.glow-on-hover {
  background: #111;
  border: none;
  outline: none;
  cursor: pointer;
  position: relative;
  z-index: 0;
}

.glow-on-hover::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #ff0057, #ffcc00, #00ffae, #00c6ff, #ff0057);
  background-size: 400%;
  z-index: -1;
  filter: blur(5px);
  animation: glowing 20s linear infinite;
  opacity: 0.8;
  border-radius: 10px;
}

@keyframes glowing {
  0% { background-position: 0 0; }
  50% { background-position: 400% 0; }
  100% { background-position: 0 0; }
}
</style>
