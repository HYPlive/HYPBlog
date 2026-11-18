<template>
	<div>
		<el-form :model="form" :rules="formRules" ref="formRef" label-position="top">
			<el-row :gutter="20">
				<el-col :span="12">
					<el-form-item label="文章标题" prop="title">
						<el-input v-model="form.title" placeholder="请输入标题"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="文章首图URL" prop="firstPicture">
						<el-input v-model="form.firstPicture" placeholder="文章首图，用于随机文章展示"></el-input>
					</el-form-item>
				</el-col>
			</el-row>

			<el-form-item label="文章描述" prop="description">
				<mavon-editor v-model="form.description"/>
			</el-form-item>

			<el-form-item label="文章正文" prop="content">
				<mavon-editor v-model="form.content"/>
			</el-form-item>

			<el-row :gutter="20">
				<el-col :span="12">
					<el-form-item label="分类" prop="cate">
						<el-select v-model="form.cate" placeholder="请选择分类（输入可添加新分类）" :allow-create="true" :filterable="true" style="width: 100%;">
							<el-option :label="item.name" :value="item.id" v-for="item in categoryList" :key="item.id"></el-option>
						</el-select>
					</el-form-item>
				</el-col>
				<el-col :span="12">
					<el-form-item label="标签" prop="tagList">
						<el-select v-model="form.tagList" placeholder="请选择标签（输入可添加新标签）" :allow-create="true" :filterable="true" :multiple="true" style="width: 100%;">
							<el-option :label="item.name" :value="item.id" v-for="item in tagList" :key="item.id"></el-option>
						</el-select>
					</el-form-item>
				</el-col>
			</el-row>

			<el-row :gutter="20">
				<el-col :span="8">
					<el-form-item label="字数" prop="words">
						<el-input v-model="form.words" placeholder="自动计算" type="number" disabled></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="8">
					<el-form-item label="阅读时长(分钟)" prop="readTime">
						<el-input v-model="form.readTime" placeholder="请输入阅读时长（可选）默认 Math.round(字数 / 200)" type="number"></el-input>
					</el-form-item>
				</el-col>
				<el-col :span="8">
					<el-form-item label="浏览次数" prop="views">
						<el-input v-model="form.views" placeholder="请输入文章字数（可选）默认为 0" type="number"></el-input>
					</el-form-item>
				</el-col>
			</el-row>

			<el-form-item style="text-align: right;">
				<el-button v-if="!$route.params.id || form.status === 'DRAFT'" @click="saveAsDraft">保存草稿</el-button>
				<el-button type="primary" @click="dialogVisible=true">保存</el-button>
			</el-form-item>
		</el-form>

		<!--编辑可见性状态对话框-->
		<el-dialog title="博客可见性" width="30%" :visible.sync="dialogVisible">
			<!--内容主体-->
			<el-form label-width="50px" @submit.native.prevent>
				<el-form-item>
					<el-radio-group v-model="radio">
						<el-radio :label="1">公开</el-radio>
						<el-radio :label="2">私密</el-radio>
						<el-radio :label="3">密码保护</el-radio>
					</el-radio-group>
				</el-form-item>
				<el-form-item label="密码" v-if="radio===3">
					<el-input v-model="form.password"></el-input>
				</el-form-item>
				<el-form-item v-if="radio!==2">
					<el-row>
						<el-col :span="6">
							<el-switch v-model="form.appreciation" active-text="赞赏"></el-switch>
						</el-col>
						<el-col :span="6">
							<el-switch v-model="form.recommend" active-text="推荐"></el-switch>
						</el-col>
						<el-col :span="6">
							<el-switch v-model="form.commentEnabled" active-text="评论"></el-switch>
						</el-col>
						<el-col :span="6">
							<el-switch v-model="form.top" active-text="置顶"></el-switch>
						</el-col>
					</el-row>
				</el-form-item>
			</el-form>
			<!--底部-->
			<span slot="footer">
				<el-button @click="dialogVisible=false">取 消</el-button>
				<el-button type="primary" @click="submit">保存</el-button>
			</span>
		</el-dialog>
	</div>
</template>

<script>
	import Breadcrumb from "@/components/Breadcrumb";
	import {getCategoryAndTag, saveBlog, getBlogById, updateBlog} from '@/api/blog'
	import {saveDraft, getDraftById, updateDraft} from '@/api/draft'

	const DEFAULT_FIRST_PICTURE = 'https://hyptest.cn/img/1.jpg'

	export default {
		name: "WriteBlog",
		components: {Breadcrumb},
		data() {
			return {
				categoryList: [],
				tagList: [],
				dialogVisible: false,
				radio: 1,
				form: {
					title: '',
					firstPicture: DEFAULT_FIRST_PICTURE,
					description: '',
					content: '',
					cate: null,
					tagList: [],
					words: null,
					readTime: null,
					views: 0,
					appreciation: false,
					recommend: false,
					commentEnabled: false,
					top: false,
					published: false,
					password: '',
					status: 'FINISHED',
				},
				formRules: {
					title: [{required: true, message: '请输入标题', trigger: 'change'}],
					firstPicture: [{required: true, message: '请输入首图链接', trigger: 'change'}],
					cate: [{required: true, message: '请选择分类', trigger: 'change'}],
					tagList: [{required: true, message: '请选择标签', trigger: 'change'}],
					words: [{required: true, message: '请输入文章字数', trigger: 'change'}],
				},
			}
		},
		watch: {
			'form.content'(val) {
				this.form.words = this.countWords(val)
				this.autoFillFirstPicture(val)
			},
			'form.words'(newValue) {
				this.form.readTime = newValue ? Math.max(1, Math.round(newValue / 200)) : null
			},
		},
		created() {
			this.getData()
			if (this.$route.params.id) {
				this.getEditData(this.$route.params.id)
			}
		},
		methods: {
			getData() {
				getCategoryAndTag().then(res => {
					this.categoryList = res.data.categories
					this.tagList = res.data.tags
				})
			},
			getEditData(id) {
				const request = this.$route.name === 'EditDraft' ? getDraftById : getBlogById
				request(id).then(res => {
					this.computeCategoryAndTag(res.data)
					this.form = res.data
					this.radio = this.form.published ? (this.form.password !== '' ? 3 : 1) : 2
				})
			},
			computeCategoryAndTag(blog) {
				blog.cate = blog.category ? blog.category.id : null
				blog.tagList = []
				;(blog.tags || []).forEach(item => {
					blog.tagList.push(item.id)
				})
			},
			countWords(content) {
				if (!content) return 0
				let text = content
				// 代码块与行内代码不计入字数
				text = text.replace(/```[\s\S]*?```/g, ' ')
				text = text.replace(/`[^`]*`/g, ' ')
				// 图片整段去掉，链接保留链接文字
				text = text.replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
				text = text.replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
				// 行首标记：标题 #、引用 >、列表 -/*/+ 或 1.、分隔线 ---
				text = text.replace(/^\s*#{1,6}\s+/gm, ' ')
				text = text.replace(/^\s*>\s?/gm, ' ')
				text = text.replace(/^\s*(?:[-*+]|\d+\.)\s+/gm, ' ')
				text = text.replace(/^\s*[-*_]{3,}\s*$/gm, ' ')
				// 行内标记：粗体、斜体、删除线
				text = text.replace(/\*\*([^*]+)\*\*/g, '$1')
				text = text.replace(/__([^_]+)__/g, '$1')
				text = text.replace(/~~([^~]+)~~/g, '$1')
				text = text.replace(/\*([^*\n]+)\*/g, '$1')
				text = text.replace(/_([^_\n]+)_/g, '$1')
				// 去掉所有空白后统计字符数
				return text.replace(/\s+/g, '').length
			},
			autoFillFirstPicture(content) {
				const isDefault = !this.form.firstPicture || this.form.firstPicture === DEFAULT_FIRST_PICTURE
				if (!isDefault) return
				const img = this.extractFirstImage(content)
				if (img) this.form.firstPicture = img
			},
			extractFirstImage(content) {
				if (!content) return ''
				let m = content.match(/!\[[^\]]*\]\(([^)\s]+)\)/)
				if (m) return m[1]
				m = content.match(/<img[^>]+src=["']([^"']+)["']/)
				if (m) return m[1]
				return ''
			},
			saveAsDraft() {
				this.form.status = 'DRAFT'
				this.form.published = false
				this.form.password = ''
				if (this.$route.name === 'EditDraft') {
					this.form.category = null
					this.form.tags = null
					updateDraft(this.form).then(res => {
						this.msgSuccess(res.msg)
						this.$router.push('/blog/draft/list')
					})
				} else {
					saveDraft(this.form).then(res => {
						this.msgSuccess(res.msg)
						this.$router.push('/blog/draft/list')
					})
				}
			},
			submit() {
				if (this.radio === 3 && (this.form.password === '' || this.form.password === null)) {
					return this.msgError("密码保护模式必须填写密码！")
				}
				this.$refs.formRef.validate(valid => {
					if (valid) {
						this.form.status = 'FINISHED'
						if (this.radio === 2) {
							this.form.appreciation = false
							this.form.recommend = false
							this.form.commentEnabled = false
							this.form.top = false
							this.form.published = false
						} else {
							this.form.published = true
						}
						if (this.radio !== 3) {
							this.form.password = ''
						}
						if (this.$route.params.id) {
							this.form.category = null
							this.form.tags = null
							updateBlog(this.form).then(res => {
								this.msgSuccess(res.msg)
								this.$router.push('/blog/list')
							})
						} else {
							saveBlog(this.form).then(res => {
								this.msgSuccess(res.msg)
								this.$router.push('/blog/list')
							})
						}
					} else {
						this.dialogVisible = false
						return this.msgError('请填写必要的表单项')
					}
				})
			}
		}
	}
</script>

<style scoped>

</style>
