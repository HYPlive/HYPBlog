<template>
	<div>
		<el-row>
			<el-col :span="8">
				<el-input placeholder="请输入标题" v-model="queryInfo.title" :clearable="true" @clear="search" @keyup.native.enter="search" size="small" style="min-width: 500px">
					<el-select v-model="queryInfo.categoryId" slot="prepend" placeholder="请选择分类" :clearable="true" @change="search" style="width: 160px">
						<el-option :label="item.name" :value="item.id" v-for="item in categoryList" :key="item.id"></el-option>
					</el-select>
					<el-button slot="append" icon="el-icon-search" @click="search"></el-button>
				</el-input>
			</el-col>
		</el-row>

		<el-table :data="draftList">
			<el-table-column label="序号" type="index" width="50"></el-table-column>
			<el-table-column label="标题" prop="title" show-overflow-tooltip></el-table-column>
			<el-table-column label="分类" prop="category.name" width="150"></el-table-column>
			<el-table-column label="字数" prop="words" width="100"></el-table-column>
			<el-table-column label="创建时间" width="170">
				<template v-slot="scope">{{ scope.row.createTime | dateFormat }}</template>
			</el-table-column>
			<el-table-column label="最近更新" width="170">
				<template v-slot="scope">{{ scope.row.updateTime | dateFormat }}</template>
			</el-table-column>
			<el-table-column label="操作" width="200">
				<template v-slot="scope">
					<el-button type="primary" icon="el-icon-edit" size="mini" @click="goDraftEditPage(scope.row.id)">编辑</el-button>
					<el-popconfirm title="确定删除该草稿吗？" icon="el-icon-delete" iconColor="red" @onConfirm="deleteDraftById(scope.row.id)">
						<el-button size="mini" type="danger" icon="el-icon-delete" slot="reference">删除</el-button>
					</el-popconfirm>
				</template>
			</el-table-column>
		</el-table>

		<el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="queryInfo.pageNum"
		               :page-sizes="[10, 20, 30, 50]" :page-size="queryInfo.pageSize" :total="total"
		               layout="total, sizes, prev, pager, next, jumper" background>
		</el-pagination>
	</div>
</template>

<script>
	import {getDataByQuery, deleteBlogById} from '@/api/blog'

	export default {
		name: "DraftList",
		data() {
			return {
				queryInfo: {
					title: '',
					categoryId: null,
					status: 'DRAFT',
					pageNum: 1,
					pageSize: 10
				},
				draftList: [],
				categoryList: [],
				total: 0
			}
		},
		created() {
			this.getData()
		},
		methods: {
			getData() {
				getDataByQuery(this.queryInfo).then(res => {
					this.draftList = res.data.blogs.list
					this.categoryList = res.data.categories
					this.total = res.data.blogs.total
				})
			},
			search() {
				this.queryInfo.pageNum = 1
				this.queryInfo.pageSize = 10
				this.getData()
			},
			handleSizeChange(newSize) {
				this.queryInfo.pageSize = newSize
				this.getData()
			},
			handleCurrentChange(newPage) {
				this.queryInfo.pageNum = newPage
				this.getData()
			},
			goDraftEditPage(id) {
				this.$router.push(`/blog/edit/${id}`)
			},
			deleteDraftById(id) {
				this.$confirm('此操作将永久删除该草稿，是否删除？', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					deleteBlogById(id).then(res => {
						this.msgSuccess(res.msg)
						this.getData()
					})
				}).catch(() => {
					this.$message({
						type: 'info',
						message: '已取消删除'
					})
				})
			}
		}
	}
</script>

<style scoped>
	.el-button + span {
		margin-left: 10px;
	}
</style>
