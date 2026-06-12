<template>
  <div class="min-h-screen bg-background text-on-background">
    <header class="topbar">
      <button class="btn-secondary" @click="$router.push('/teacher/dashboard')">
        <span class="material-symbols-outlined">arrow_back</span>
        Exit Configuration
      </button>
      <div class="headline-md">New Midterm Assessment</div>
      <div class="label flex items-center gap-1">
        <span class="material-symbols-outlined text-[16px]">cloud_done</span>
        Draft auto-saved
      </div>
    </header>

    <main class="grid min-h-[calc(100vh-64px)] grid-cols-[minmax(0,1fr)_360px] overflow-hidden">
      <section class="overflow-y-auto p-gutter">
        <div class="mx-auto max-w-3xl pb-28">
          <div class="mb-12 flex items-center justify-between px-4">
            <div v-for="(step, index) in steps" :key="step" class="flex flex-col items-center gap-2">
              <div class="grid h-8 w-8 place-items-center rounded-full border-4 border-background" :class="index === 0 ? 'bg-primary text-on-primary ring-2 ring-primary' : 'bg-surface-container-highest text-on-surface-variant'">
                {{ index + 1 }}
              </div>
              <span class="label" :class="index === 0 ? 'text-primary' : 'text-on-surface-variant'">{{ step }}</span>
            </div>
          </div>

          <section class="panel overflow-hidden">
            <div class="panel-header">
              <h1 class="headline-md flex items-center gap-2">
                <span class="material-symbols-outlined text-primary">settings</span>
                Exam Parameters
              </h1>
              <p class="muted mt-1">Define the fundamental settings and timing for this assessment.</p>
            </div>
            <div class="grid gap-8 p-6">
              <label class="grid gap-2">
                <span class="label text-on-surface">Exam Title</span>
                <input v-model="form.examName" class="form-input" />
              </label>
              <div class="grid gap-6 md:grid-cols-2">
                <label class="grid gap-2">
                  <span class="label text-on-surface">Start Window</span>
                  <input v-model="form.startTime" class="form-input" type="datetime-local" />
                </label>
                <label class="grid gap-2">
                  <span class="label text-on-surface">End Window</span>
                  <input v-model="form.endTime" class="form-input" type="datetime-local" />
                </label>
              </div>
              <div class="grid gap-6 md:grid-cols-2">
                <label class="grid gap-2">
                  <span class="label text-on-surface">Duration Limit (Minutes)</span>
                  <input v-model.number="form.durationMinutes" class="form-input" type="number" />
                </label>
                <label class="grid gap-2">
                  <span class="label text-on-surface">Lockdown Browser</span>
                  <div class="flex items-center justify-between rounded-lg border border-outline-variant bg-surface-bright px-4 py-3">
                    <span class="flex items-center gap-2">
                      <span class="material-symbols-outlined">lock</span>
                      Enabled
                    </span>
                    <input v-model="form.lockdownEnabled" type="checkbox" />
                  </div>
                </label>
              </div>
              <label class="grid gap-2">
                <span class="label text-on-surface">Instructions</span>
                <textarea v-model="form.instructions" class="form-input min-h-28 resize-y"></textarea>
              </label>
            </div>
          </section>
        </div>
      </section>

      <aside class="border-l border-outline-variant bg-surface-container-lowest p-6">
        <div class="label mb-4">Live Preview</div>
        <section class="panel p-5">
          <div class="difficulty">DRAFT</div>
          <h2 class="headline-md mt-4">{{ form.examName }}</h2>
          <p class="muted mt-2">{{ form.instructions }}</p>
          <div class="mt-6 grid gap-3 text-sm">
            <div class="metric"><span class="material-symbols-outlined">calendar_today</span><span>{{ form.startTime }}</span></div>
            <div class="metric"><span class="material-symbols-outlined">event_busy</span><span>{{ form.endTime }}</span></div>
            <div class="metric"><span class="material-symbols-outlined">timer</span><span>{{ form.durationMinutes }} minutes</span></div>
          </div>
          <button class="btn-primary mt-6 w-full" @click="save">Save Draft</button>
        </section>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

const steps = ['Configuration', 'Select Problems', 'Assign Points']
const form = reactive({
  examName: 'CS304 Database Systems - Midterm',
  startTime: '2026-06-15T09:00',
  endTime: '2026-06-15T11:00',
  durationMinutes: 120,
  lockdownEnabled: true,
  instructions: '请使用标准 MySQL 语法完成题目。',
})

function save() {
  // The backend API can be wired here when exam creation is expanded.
}
</script>
