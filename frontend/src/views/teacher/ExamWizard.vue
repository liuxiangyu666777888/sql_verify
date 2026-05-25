<template>
  <div class="app-shell min-h-screen p-6">
    <div class="max-w-5xl mx-auto panel p-6 space-y-4">
      <h1 class="text-2xl font-bold">新建考试</h1>
      <input class="w-full border rounded-lg px-4 py-3" placeholder="考试标题" />
      <div class="grid md:grid-cols-2 gap-4">
        <input type="datetime-local" class="w-full border rounded-lg px-4 py-3" />
        <input type="datetime-local" class="w-full border rounded-lg px-4 py-3" />
      </div>
      <textarea class="w-full border rounded-lg px-4 py-3 min-h-[160px]" placeholder="考试说明" />
      <button class="btn-primary rounded-lg px-4 py-2">保存草稿</button>
    </div>
  </div>
</template>
