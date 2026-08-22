<template>
	<div class="todo-page">
		<div class="todo-heading">
			<h2>Todo</h2>
			<p>把想做的事，一件一件完成。</p>
		</div>
		<div class="todo-columns">
			<section v-for="column in columns" :key="column.status" class="todo-column">
				<header><span :class="['status-mark', column.status.toLowerCase()]"></span><h3>{{ column.title }}</h3><strong>{{ column.items.length }}</strong></header>
				<div v-if="column.items.length" class="todo-items">
					<article v-for="todo in column.items" :key="todo.id" :class="['todo-item', {high: todo.priority >= 4}]">
						<span :class="['status-mark', column.status.toLowerCase()]"></span>
						<div>
							<p>{{ todo.content }}</p>
							<div class="todo-info">
								<span class="stars">{{ starText(todo.priority) }}</span>
								<span>发现 {{ todo.discoveredTime | dateFormat('YYYY-MM-DD HH:mm') }}</span>
								<span v-if="todo.completedTime">完成 {{ todo.completedTime | dateFormat('YYYY-MM-DD HH:mm') }}</span>
								<span>持续 {{ todo.durationMinutes }} 分钟</span>
							</div>
						</div>
					</article>
				</div>
				<p v-else class="empty">暂无事项</p>
			</section>
		</div>
		<div class="todo-summary">共 {{ total }} 条 Todo，{{ inProgressCount }} 条进行中，{{ unfinishedCount }} 条未完成</div>
	</div>
</template>

<script>
	import {getTodoList} from '@/api/todo'

	export default {
		name: 'Todo',
		data() {
			return {
				columns: [
					{status: 'TODO', title: 'Todo', items: []},
					{status: 'IN_PROGRESS', title: 'Doing', items: []},
					{status: 'DONE', title: 'Done', items: []}
				]
			}
		},
		computed: {
			total() { return this.columns.reduce((sum, column) => sum + column.items.length, 0) },
			inProgressCount() { return this.columns[1].items.length },
			unfinishedCount() { return this.columns[0].items.length + this.columns[1].items.length }
		},
		created() {
			getTodoList().then(res => this.columns.forEach(column => { column.items = res.data.filter(todo => todo.status === column.status) })).catch(() => this.msgError('Todo加载失败'))
		},
		methods: {
			starText(priority) { return '★'.repeat(priority) + '☆'.repeat(5 - priority) }
		}
	}
</script>

<style scoped>
	.todo-heading { margin-bottom: 22px; }
	.todo-heading h2 { margin-bottom: 5px; }
	.todo-heading p { color: #909399; margin: 0; }
	.todo-columns { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
	.todo-column { min-height: 360px; background: rgba(255, 255, 255, .9); border: 1px solid #edf0f5; border-radius: 6px; }
	.todo-column header { display: flex; align-items: center; gap: 8px; padding: 14px 15px; border-bottom: 1px solid #edf0f5; }
	.todo-column h3 { margin: 0; font-size: 16px; }
	.todo-column header strong { margin-left: auto; color: #909399; }
	.todo-items { padding: 10px; }
	.todo-item { display: flex; gap: 8px; padding: 12px; margin-bottom: 9px; border: 1px solid #edf0f5; border-radius: 4px; background: #fff; }
	.todo-item.high { border-left: 3px solid #f56c6c; }
	.todo-item p { margin: 0 0 9px; line-height: 1.5; word-break: break-word; }
	.todo-info { display: flex; flex-wrap: wrap; gap: 5px 10px; color: #a0a5ad; font-size: 11px; }
	.stars { color: #e6a23c; letter-spacing: 0; }
	.status-mark { display: inline-block; flex: 0 0 auto; width: 8px; height: 8px; margin-top: 6px; border-radius: 50%; }
	.status-mark.todo { background: #e6a23c; }
	.status-mark.in_progress { background: #409eff; }
	.status-mark.done { background: #67c23a; }
	.empty { padding: 35px 15px; text-align: center; color: #c0c4cc; }
	.todo-summary { margin: 20px 0; padding: 15px; text-align: center; color: #606266; background: rgba(255, 255, 255, .86); border-top: 1px solid #edf0f5; }
	@media (max-width: 768px) { .todo-columns { grid-template-columns: 1fr; } .todo-column { min-height: auto; } }
</style>
