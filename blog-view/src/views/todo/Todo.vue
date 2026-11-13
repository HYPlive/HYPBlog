<template>
	<div class="todo-page">
		<div class="todo-heading">
			<h2>Todo</h2>
			<div class="heading-row">
				<p>把想做的事，一件一件完成。</p>
				<el-select v-model="sortType" size="small" class="sort-select">
					<el-option label="默认排序" value="default"></el-option>
					<el-option label="按星级" value="priority"></el-option>
					<el-option label="按时间" value="time"></el-option>
				</el-select>
			</div>
		</div>
		<div class="todo-columns">
			<section v-for="column in columns" :key="column.status" class="todo-column">
				<header><span :class="['status-mark', column.status.toLowerCase()]"></span><h3>{{ column.title }}</h3><strong>{{ column.items.length }}</strong></header>
				<div v-if="column.items.length" class="todo-items">
					<article v-for="todo in pageItems(column)" :key="todo.id" :class="['todo-item', {high: todo.priority >= 4}]">
						<span :class="['status-mark', column.status.toLowerCase()]"></span>
						<div>
							<p>{{ todo.content }}</p>
							<div class="stars">{{ starText(todo.priority) }}</div>
							<div class="todo-info">
								<span>创建 {{ todo.discoveredTime | dateFormat('YYYY-MM-DD HH:mm') }}</span>
								<span v-if="todo.completedTime">完成 {{ todo.completedTime | dateFormat('YYYY-MM-DD HH:mm') }}</span>
								<span>持续 {{ formatDuration(todo.durationMinutes) }}</span>
							</div>
						</div>
					</article>
				</div>
				<p v-else class="empty">暂无事项</p>
				<div v-if="column.items.length > pageSize" class="pager">
					<el-pagination small background layout="prev, pager, next" :total="column.items.length" :page-size="pageSize" :current-page.sync="column.page"></el-pagination>
				</div>
			</section>
		</div>
		<div class="todo-summary">共 {{ total }} 条 Todo，{{ inProgressCount }} 条进行中，{{ unfinishedCount }} 条未完成</div>
	</div>
</template>

<script>
	import {getTodoList} from '@/api/todo'
	import moment from 'moment'

	export default {
		name: 'Todo',
		data() {
			return {
				pageSize: 10,
				sortType: 'default',
				columns: [
					{status: 'TODO', title: 'Todo', items: [], page: 1},
					{status: 'IN_PROGRESS', title: 'Doing', items: [], page: 1},
					{status: 'DONE', title: 'Done', items: [], page: 1}
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
		watch: {
			sortType() { this.columns.forEach(column => { column.page = 1 }) }
		},
		methods: {
			sortedItems(column) {
				const items = column.items.slice()
				if (this.sortType === 'priority') {
					items.sort((a, b) => b.priority !== a.priority ? b.priority - a.priority : this.timeOf(a) - this.timeOf(b))
				} else if (this.sortType === 'time') {
					items.sort((a, b) => this.timeOf(a) - this.timeOf(b))
				}
				return items
			},
			timeOf(todo) { return todo.discoveredTime ? moment(todo.discoveredTime).valueOf() : 0 },
			pageItems(column) { return this.sortedItems(column).slice((column.page - 1) * this.pageSize, column.page * this.pageSize) },
			starText(priority) { return '★'.repeat(priority) + '☆'.repeat(5 - priority) },
			formatDuration(minutes) {
				if (minutes == null || minutes < 0) return ''
				if (minutes < 60) return minutes + ' 分钟'
				if (minutes < 1440) {
					const h = Math.floor(minutes / 60)
					const m = minutes % 60
					return m > 0 ? h + ' 小时 ' + m + ' 分钟' : h + ' 小时'
				}
				const d = Math.floor(minutes / 1440)
				const h = Math.floor((minutes % 1440) / 60)
				return h > 0 ? d + ' 天 ' + h + ' 小时' : d + ' 天'
			}
		}
	}
</script>

<style scoped>
	.todo-heading { margin-bottom: 22px; }
	.todo-heading h2 { margin-bottom: 5px; }
	.todo-heading p { color: #fff; font-size: 18px; margin: 0; }
	.heading-row { display: flex; align-items: center; justify-content: space-between; margin-top: -6px; }
	.sort-select { width: 120px; }
	.todo-columns { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
	.todo-column { min-height: 360px; background: rgba(255, 255, 255, .9); border: 1px solid #edf0f5; border-radius: 6px; }
	.todo-column header { display: flex; align-items: center; gap: 8px; padding: 14px 15px; border-bottom: 1px solid #edf0f5; }
	.todo-column h3 { margin: 0; font-size: 16px; }
	.todo-column header strong { margin-left: auto; color: #909399; }
	.todo-items { padding: 10px; }
	.todo-item { display: flex; gap: 8px; padding: 12px; margin-bottom: 9px; border: 1px solid #edf0f5; border-radius: 4px; background: #fff; }
	.todo-item.high { border-left: 3px solid #e6a23c; }
	.todo-item p { margin: 0 0 9px; line-height: 1.5; word-break: break-word; }
	.todo-info { display: flex; flex-wrap: wrap; gap: 5px 10px; color: #a0a5ad; font-size: 11px; }
	.stars { color: #e6a23c; letter-spacing: 0; margin-bottom: 6px; }
	.status-mark { display: inline-block; flex: 0 0 auto; width: 8px; height: 8px; margin-top: 6px; border-radius: 50%; }
	.status-mark.todo { background: #e6a23c; }
	.status-mark.in_progress { background: #409eff; }
	.status-mark.done { background: #67c23a; }
	.empty { padding: 35px 15px; text-align: center; color: #c0c4cc; }
	.pager { padding: 8px 10px 12px; text-align: center; }
	.todo-summary { margin: 20px 0; padding: 15px; text-align: center; color: #606266; background: rgba(255, 255, 255, .86); border-top: 1px solid #edf0f5; }
	@media (max-width: 768px) { .todo-columns { grid-template-columns: 1fr; } .todo-column { min-height: auto; } }
</style>
