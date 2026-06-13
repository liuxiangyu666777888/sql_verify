<template>
  <div class="flex flex-col items-center">
    <div class="relative h-32 w-32">
      <svg class="h-full w-full -rotate-90" viewBox="0 0 36 36">
        <circle
          cx="18" cy="18" r="15.9"
          fill="none"
          stroke="currentColor"
          stroke-width="3.8"
          class="text-surface-highest"
        />
        <circle
          cx="18" cy="18" r="15.9"
          fill="none"
          stroke="currentColor"
          stroke-width="2.8"
          stroke-linecap="round"
          class="text-primary"
          :stroke-dasharray="`${percentage}, 100`"
        />
      </svg>
      <div class="absolute inset-0 flex flex-col items-center justify-center">
        <span class="text-[32px] font-extrabold text-on-surface">{{ value }}</span>
        <span class="text-xs font-semibold text-on-surface-variant">{{ label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  value: number
  total: number
  label: string
}>()

const percentage = computed(() =>
  props.total > 0 ? Math.round((props.value / props.total) * 100) : 0,
)
</script>
