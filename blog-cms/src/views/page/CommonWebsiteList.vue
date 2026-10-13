<template>
	<div>
		<!--添加-->
		<el-form inline>
			<el-form-item>
				<el-button type="primary" size="small" icon="el-icon-plus" @click="addDialogVisible=true">添加网站</el-button>
			</el-form-item>
		</el-form>

		<el-table :data="websiteList">
			<el-table-column label="序号" type="index" width="50"></el-table-column>
			<el-table-column label="图标" width="70">
				<template v-slot="scope">
					<span class="icon-badge" :style="{background: scope.row.color || '#48dbfb'}">
						<img v-if="isImageUrl(scope.row.icon)" :src="scope.row.icon" alt="">
						<span v-else-if="scope.row.icon">{{ scope.row.icon }}</span>
						<i v-else class="el-icon-link"></i>
					</span>
				</template>
			</el-table-column>
			<el-table-column label="名称" prop="name"></el-table-column>
			<el-table-column label="介绍" prop="description" show-overflow-tooltip></el-table-column>
			<el-table-column label="网址" prop="url" show-overflow-tooltip></el-table-column>
			<el-table-column label="主题色" width="90">
				<template v-slot="scope">
					<span v-if="scope.row.color" class="color-swatch" :style="{background: scope.row.color}">{{ scope.row.color }}</span>
					<span v-else>默认</span>
				</template>
			</el-table-column>
			<el-table-column label="排序" prop="sort" width="70"></el-table-column>
			<el-table-column label="是否公开" width="100">
				<template v-slot="scope">
					<el-switch v-model="scope.row.published" @change="publishedChanged(scope.row)"></el-switch>
				</template>
			</el-table-column>
			<el-table-column label="操作" width="200">
				<template v-slot="scope">
					<el-button type="primary" icon="el-icon-edit" size="mini" @click="showEditDialog(scope.row)">编辑</el-button>
					<el-popconfirm title="确定删除吗？" icon="el-icon-delete" iconColor="red" @onConfirm="deleteWebsiteById(scope.row.id)">
						<el-button size="mini" type="danger" icon="el-icon-delete" slot="reference">删除</el-button>
					</el-popconfirm>
				</template>
			</el-table-column>
		</el-table>

		<!--分页-->
		<el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="queryInfo.pageNum"
		               :page-sizes="[10, 20, 30, 50]" :page-size="queryInfo.pageSize" :total="total"
		               layout="total, sizes, prev, pager, next, jumper" background>
		</el-pagination>

		<!--添加网站对话框-->
		<el-dialog title="添加网站" width="40%" :visible.sync="addDialogVisible" :close-on-click-modal="false" @close="addDialogClosed">
			<el-form :model="addForm" :rules="formRules" ref="addFormRef" label-width="80px">
				<el-form-item label="名称" prop="name">
					<el-input v-model="addForm.name"></el-input>
				</el-form-item>
				<el-form-item label="介绍" prop="description">
					<el-input v-model="addForm.description"></el-input>
				</el-form-item>
				<el-form-item label="网址" prop="url">
					<el-input v-model="addForm.url" placeholder="https://..."></el-input>
				</el-form-item>
				<el-form-item label="图标" prop="icon">
					<el-input v-model="addForm.icon" placeholder="图片URL或emoji，如 🐙 或 https://github.com/favicon.ico"></el-input>
				</el-form-item>
				<el-form-item label="主题色" prop="color">
					<el-color-picker v-model="addForm.color"></el-color-picker>
				</el-form-item>
				<el-form-item label="排序" prop="sort">
					<el-input-number v-model="addForm.sort" :min="0" :max="9999"></el-input-number>
				</el-form-item>
				<el-form-item label="是否公开" prop="published">
					<el-switch v-model="addForm.published"></el-switch>
				</el-form-item>
			</el-form>
			<span slot="footer">
				<el-button @click="addDialogVisible=false">取 消</el-button>
				<el-button type="primary" @click="saveWebsite">确 定</el-button>
			</span>
		</el-dialog>

		<!--编辑网站对话框-->
		<el-dialog title="编辑网站" width="40%" :visible.sync="editDialogVisible" :close-on-click-modal="false" @close="editDialogClosed">
			<el-form :model="editForm" :rules="formRules" ref="editFormRef" label-width="80px">
				<el-form-item label="名称" prop="name">
					<el-input v-model="editForm.name"></el-input>
				</el-form-item>
				<el-form-item label="介绍" prop="description">
					<el-input v-model="editForm.description"></el-input>
				</el-form-item>
				<el-form-item label="网址" prop="url">
					<el-input v-model="editForm.url" placeholder="https://..."></el-input>
				</el-form-item>
				<el-form-item label="图标" prop="icon">
					<el-input v-model="editForm.icon" placeholder="图片URL或emoji，如 🐙 或 https://github.com/favicon.ico"></el-input>
				</el-form-item>
				<el-form-item label="主题色" prop="color">
					<el-color-picker v-model="editForm.color"></el-color-picker>
				</el-form-item>
				<el-form-item label="排序" prop="sort">
					<el-input-number v-model="editForm.sort" :min="0" :max="9999"></el-input-number>
				</el-form-item>
				<el-form-item label="是否公开" prop="published">
					<el-switch v-model="editForm.published"></el-switch>
				</el-form-item>
			</el-form>
			<span slot="footer">
				<el-button @click="editDialogVisible=false">取 消</el-button>
				<el-button type="primary" @click="editWebsite">确 定</el-button>
			</span>
		</el-dialog>
	</div>
</template>

<script>
	import Breadcrumb from "@/components/Breadcrumb";
	import {getWebsitesByQuery, updatePublished, saveWebsite, updateWebsite, deleteWebsiteById} from "@/api/commonWebsite";

	export default {
		name: "CommonWebsiteList",
		components: {Breadcrumb},
		data() {
			return {
				queryInfo: {
					pageNum: 1,
					pageSize: 10
				},
				websiteList: [],
				total: 0,
				addDialogVisible: false,
				editDialogVisible: false,
				addForm: {
					name: '',
					description: '',
					url: '',
					icon: '',
					color: '',
					published: true,
					sort: 0
				},
				editForm: {
					name: '',
					description: '',
					url: '',
					icon: '',
					color: '',
					published: true,
					sort: 0
				},
				formRules: {
					name: [{required: true, message: '请输入名称', trigger: 'blur'}],
					url: [{required: true, message: '请输入网址', trigger: 'blur'}],
				}
			}
		},
		created() {
			this.getWebsiteList()
		},
		methods: {
			isImageUrl(icon) {
				return icon && (icon.startsWith('http') || icon.startsWith('/'))
			},
			getWebsiteList() {
				getWebsitesByQuery(this.queryInfo).then(res => {
					this.websiteList = res.data.list
					this.total = res.data.total
				})
			},
			handleSizeChange(newSize) {
				this.queryInfo.pageSize = newSize
				this.getWebsiteList()
			},
			handleCurrentChange(newPage) {
				this.queryInfo.pageNum = newPage
				this.getWebsiteList()
			},
			publishedChanged(row) {
				updatePublished(row.id, row.published).then(res => {
					this.msgSuccess(res.msg)
				})
			},
			deleteWebsiteById(id) {
				deleteWebsiteById(id).then(res => {
					this.getWebsiteList()
					this.msgSuccess(res.msg)
				})
			},
			showEditDialog(row) {
				this.editForm = {...row}
				this.editDialogVisible = true
			},
			addDialogClosed() {
				this.$refs.addFormRef.resetFields()
			},
			editDialogClosed() {
				this.editForm = {}
				this.$refs.editFormRef.resetFields()
			},
			saveWebsite() {
				this.$refs.addFormRef.validate(valid => {
					if (valid) {
						saveWebsite(this.addForm).then(res => {
							this.getWebsiteList()
							this.msgSuccess(res.msg)
							this.addDialogVisible = false
						})
					}
				})
			},
			editWebsite() {
				this.$refs.editFormRef.validate(valid => {
					if (valid) {
						updateWebsite(this.editForm).then(res => {
							this.getWebsiteList()
							this.msgSuccess(res.msg)
							this.editDialogVisible = false
						})
					}
				})
			}
		}
	}
</script>

<style scoped>
	.icon-badge {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		width: 32px;
		height: 32px;
		border-radius: 8px;
		color: #fff;
		font-size: 16px;
	}

	.icon-badge img {
		width: 20px;
		height: 20px;
		border-radius: 4px;
		object-fit: contain;
	}

	.color-swatch {
		display: inline-block;
		max-width: 70px;
		padding: 1px 5px;
		border-radius: 3px;
		color: #fff;
		font-size: 11px;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
</style>
