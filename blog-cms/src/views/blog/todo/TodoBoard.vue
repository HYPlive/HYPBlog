<template>
	<div class="todo-board">
		<div class="toolbar">
			<el-button type="primary" icon="el-icon-plus" @click="openAdd">添加 Todo</el-button>
			<el-radio-group v-model="sortType" size="small">
				<el-radio-button label="default">默认</el-radio-button>
				<el-radio-button label="priority">星级</el-radio-button>
				<el-radio-button label="time">时间</el-radio-button>
			</el-radio-group>
		</div>
		<div class="board-columns">
			<section v-for="column in columns" :key="column.status" class="todo-column">
				<header><span :class="['status-mark', column.status.toLowerCase()]"></span><h3>{{ column.title }}</h3><small>{{ column.items.length }}</small></header>
				<div class="todo-list" @dragover.prevent @drop="dropTodo($event, column.status)">
					<article v-for="todo in pageItems(column)" :key="todo.id" class="todo-item" :data-id="todo.id" draggable="true" @dragstart="startDrag(todo, column.status)" @dblclick="openEdit(todo)">
						<span :class="['status-mark', column.status.toLowerCase()]"></span>
						<div class="todo-content"><p>{{ todo.content }}</p><div class="todo-meta"><el-rate v-model="todo.priority" disabled></el-rate><span>{{ todo.discoveredTime | dateFormat('MM-DD HH:mm') }}</span><el-radio-group v-model="todo.published" @change="changePublished(todo)"><el-radio :label="true">公开</el-radio><el-radio :label="false">私有</el-radio></el-radio-group></div></div>
						<el-button type="text" icon="el-icon-delete" @click="removeTodo(todo.id)"></el-button>
					</article>
					<p v-if="column.items.length === 0" class="empty">暂无事项</p>
				</div>
				<div v-if="column.items.length > pageSize" class="pager">
					<el-pagination small background layout="prev, pager, next" :total="column.items.length" :page-size="pageSize" :current-page.sync="column.page"></el-pagination>
				</div>
			</section>
		</div>
		<el-dialog title="编辑 Todo" :visible.sync="dialogVisible" width="460px" @close="editTodo = {}">
			<el-form v-if="dialogVisible" label-position="top"><el-form-item label="事项"><el-input v-model.trim="editTodo.content" type="textarea" :rows="3" maxlength="500" show-word-limit></el-input></el-form-item><el-form-item label="星级"><el-rate v-model="editTodo.priority"></el-rate></el-form-item><el-form-item label="公开"><el-radio-group v-model="editTodo.published"><el-radio :label="true">前台公开</el-radio><el-radio :label="false">仅后台可见</el-radio></el-radio-group></el-form-item></el-form>
			<span slot="footer"><el-button @click="dialogVisible = false">取 消</el-button><el-button type="primary" @click="saveEdit">保 存</el-button></span>
		</el-dialog>
		<el-dialog title="添加 Todo" :visible.sync="addVisible" width="460px" @close="resetAdd">
			<el-form v-if="addVisible" label-position="top"><el-form-item label="事项"><el-input v-model.trim="addForm.content" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="输入待办事项"></el-input></el-form-item><el-form-item label="星级"><el-rate v-model="addForm.priority"></el-rate></el-form-item><el-form-item label="公开"><el-radio-group v-model="addForm.published"><el-radio :label="true">前台公开</el-radio><el-radio :label="false">仅后台可见</el-radio></el-radio-group></el-form-item></el-form>
			<span slot="footer"><el-button @click="addVisible = false">取 消</el-button><el-button type="primary" @click="submitAdd">保 存</el-button></span>
		</el-dialog>
	</div>
</template>

<script>
	import {deleteTodoById, getTodoList, saveTodo, updatePublished, updateTodo, updateTodoBoard} from '@/api/todo'
	import moment from 'moment'

	export default {
		name: 'TodoBoard',
		data() {
			return {pageSize: 10, sortType: 'default', addVisible: false, addForm: {content: '', priority: 1, published: true}, dialogVisible: false, editTodo: {}, draggedTodo: null, draggedStatus: '', columns: [
				{status: 'TODO', title: 'Todo', items: [], page: 1}, {status: 'IN_PROGRESS', title: 'Doing', items: [], page: 1}, {status: 'DONE', title: 'Done', items: [], page: 1}
			]}
		},
		created() { this.loadTodos() },
		watch: { sortType() { this.columns.forEach(column => { column.page = 1 }) } },
		methods: {
			loadTodos() { getTodoList().then(res => this.columns.forEach(column => { column.items = res.data.filter(todo => todo.status === column.status); this.normalizePage(column) })) },
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
			normalizePage(column) { column.page = Math.min(column.page, Math.max(1, Math.ceil(column.items.length / this.pageSize))) },
			openAdd() { this.addForm = {content: '', priority: 1, published: true}; this.addVisible = true },
			resetAdd() { this.addForm = {content: '', priority: 1, published: true} },
			submitAdd() { if (!this.addForm.content) return; saveTodo(this.addForm).then(res => { this.addVisible = false; this.msgSuccess(res.msg); this.columns[0].page = 1; this.loadTodos() }) },
			openEdit(todo) { this.editTodo = {...todo}; this.dialogVisible = true },
			saveEdit() { if (!this.editTodo.content) return; updateTodo(this.editTodo).then(res => { this.dialogVisible = false; this.msgSuccess(res.msg); this.loadTodos() }) },
			changePublished(todo) { updatePublished(todo.id, todo.published).then(res => this.msgSuccess(res.msg)).catch(() => this.loadTodos()) },
			removeTodo(id) { this.$confirm('确定删除这条 Todo 吗？', '提示', {type: 'warning'}).then(() => deleteTodoById(id)).then(res => { this.msgSuccess(res.msg); this.loadTodos() }).catch(() => {}) },
			startDrag(todo, status) { this.draggedTodo = todo; this.draggedStatus = status },
			dropTodo(event, targetStatus) {
				if (!this.draggedTodo) return
				const source = this.columns.find(column => column.status === this.draggedStatus)
				const target = this.columns.find(column => column.status === targetStatus)
				source.items = source.items.filter(todo => todo.id !== this.draggedTodo.id)
				this.normalizePage(source)
				const cards = Array.from(event.currentTarget.querySelectorAll('.todo-item'))
				const targetCard = cards.find(card => event.clientY < card.getBoundingClientRect().top + card.getBoundingClientRect().height / 2)
				let insertIndex
				if (targetCard) {
					const targetId = Number(targetCard.dataset.id)
					insertIndex = target.items.findIndex(todo => todo.id === targetId)
					if (insertIndex === -1) insertIndex = target.items.length
				} else {
					insertIndex = target.items.length
				}
				target.items.splice(insertIndex, 0, this.draggedTodo)
				this.normalizePage(target)
				const affected = source === target ? [target] : [source, target]
				updateTodoBoard(affected.map(column => ({status: column.status, todoIds: column.items.map(todo => todo.id)}))).then(() => this.loadTodos()).catch(() => this.loadTodos())
				this.draggedTodo = null
			}
		}
	}
</script>

<style scoped>
	.toolbar { margin-bottom: 24px; display: flex; align-items: center; gap: 16px; }
	.board-columns { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 18px; }
	.todo-column { min-height: 430px; background: #f7f9fc; border: 1px solid #e5eaf3; border-radius: 6px; }
	.todo-column header { display: flex; align-items: center; gap: 8px; padding: 14px 16px; border-bottom: 1px solid #e5eaf3; }
	.todo-column h3 { margin: 0; font-size: 15px; } .todo-column small { margin-left: auto; color: #909399; }
	.todo-list { min-height: 360px; padding: 10px; } .todo-item { display: flex; gap: 8px; align-items: flex-start; margin-bottom: 10px; padding: 12px; background: #fff; border: 1px solid #e5eaf3; border-radius: 4px; cursor: grab; }
	.todo-content { min-width: 0; flex: 1; } .todo-content p { margin: 0 0 10px; line-height: 1.5; word-break: break-word; }
	.todo-meta { display: flex; gap: 8px; align-items: center; color: #909399; font-size: 12px; } .todo-meta .el-rate { display: inline-flex; } .todo-meta .el-rate /deep/ .el-rate__icon { font-size: 14px; margin-right: 1px; } .todo-meta /deep/ .el-radio { margin-right: 8px; } .todo-meta /deep/ .el-radio__label { font-size: 12px; }
	.status-mark { display: inline-block; flex: 0 0 auto; width: 8px; height: 8px; margin-top: 6px; border-radius: 50%; } .status-mark.todo { background: #e6a23c; } .status-mark.in_progress { background: #409eff; } .status-mark.done { background: #67c23a; }
	.empty { color: #909399; text-align: center; padding-top: 32px; }
	.pager { padding: 8px 10px 14px; text-align: center; }
	@media (max-width: 960px) { .board-columns { grid-template-columns: 1fr; } .todo-column { min-height: auto; } .todo-list { min-height: 100px; } }
</style>
