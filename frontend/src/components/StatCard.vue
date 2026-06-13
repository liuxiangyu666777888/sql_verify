<template>
  <div class="stat-card relative overflow-hidden rounded-xl border border-outline-variant bg-surface-lowest p-6 shadow-sm">
    <span class="material-symbols-outlined absolute right-4 top-4 text-[64px] text-on-surface opacity-10">
      {{ icon }}
    </span>
    <div class="label text-xs font-bold uppercase tracking-widest text-on-surface-variant">
      {{ label }}
    </div>
    <div class="mt-2 text-[32px] font-extrabold leading-10" :class="valueClass">
      {{ value }}
    </div>
    <div v-if="sub" class="mt-3 flex items-center gap-1 text-xs font-semibold" :class="subClass">
      <span v-if="subIcon" class="material-symbols-outlined text-[16px]">{{ subIcon }}</span>
      {{ sub }}
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  icon: string
  label: string
  value: string | number
  sub?: string
  subIcon?: string
  valueClass?: string
  subClass?: string
}>()
</script>
