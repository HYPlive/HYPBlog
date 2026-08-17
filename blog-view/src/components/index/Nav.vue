<template>
	<div ref="nav" class="ui fixed inverted stackable pointing menu" :class="{'transparent':$route.name==='home' && clientSize.clientWidth>768}">
		<div class="ui container">
			<router-link to="/">
				<h3 class="ui header item m-blue">{{ blogName }}</h3>
			</router-link>
			<router-link to="/home" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='home'}">
				<i class="home icon"></i>首页
			</router-link>
			<el-dropdown trigger="click" @command="categoryRoute">
				<span class="el-dropdown-link item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='category'}">
					<i class="idea icon"></i>分类<i class="caret down icon"></i>
				</span>
				<el-dropdown-menu slot="dropdown">
					<el-dropdown-item :command="category.name" v-for="(category,index) in categoryList" :key="index">{{ category.name }}</el-dropdown-item>
				</el-dropdown-menu>
			</el-dropdown>
			<router-link to="/archives" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='archives'}">
				<i class="clone icon"></i>归档
			</router-link>
			<router-link to="/moments" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='moments'}">
				<i class="comment alternate outline icon"></i>动态
			</router-link>
			<router-link to="/friends" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='friends'}">
				<i class="users icon"></i>友人帐
			</router-link>
			<router-link to="/about" class="item" :class="{'m-mobile-hide': mobileHide,'active':$route.name==='about'}">
				<i class="info icon"></i>关于我
			</router-link>
			<el-autocomplete v-model="queryString" :fetch-suggestions="debounceQuery" placeholder="Search..."
			                 class="right item m-search" :class="{'m-mobile-hide': mobileHide}"
			                 popper-class="m-search-item" @select="handleSelect">
				<i class="search icon el-input__icon" slot="suffix"></i>
				<template slot-scope="{ item }">
					<div class="title">{{ item.title }}</div>
					<span class="content">{{ item.content }}</span>
				</template>
			</el-autocomplete>
			<button class="ui menu black icon button m-right-top m-mobile-show" @click="toggle">
				<i class="sidebar icon"></i>
			</button>
		</div>
	</div>
</template>

<script>
	import {getSearchBlogList} from "@/api/blog";
	import {mapState} from 'vuex'

	export default {
		name: "Nav",
		props: {
			blogName: {
				type: String,
				required: true
			},
			categoryList: {
				type: Array,
				required: true
			},
		},
		data() {
			return {
				mobileHide: true,
				queryString: '',
				queryResult: [],
				timer: null
			}
		},
		computed: {
			...mapState(['clientSize'])
		},
		watch: {
			//路由改变时，收起导航栏
			'$route.path'() {
				this.mobileHide = true
			}
		},
		mounted() {
			//监听页面滚动位置，改变导航栏的显示
			window.addEventListener('scroll', () => {
				//首页且不是移动端
				if (this.$route.name === 'home' && this.clientSize.clientWidth > 768) {
					if (window.scrollY > this.clientSize.clientHeight / 2) {
						this.$refs.nav.classList.remove('transparent')
					} else {
						this.$refs.nav.classList.add('transparent')
					}
				}
			})
			//监听点击事件，收起导航菜单
			document.addEventListener('click', (e) => {
				//遍历冒泡
				let flag = this.$refs.nav.contains(e.target)
				//如果导航栏是打开状态，且点击的元素不是Nav的子元素，则收起菜单
				if (!this.mobileHide && !flag) {
					this.mobileHide = true
				}
			})
		},
		methods: {
			toggle() {
				this.mobileHide = !this.mobileHide
			},
			categoryRoute(name) {
				this.$router.push(`/category/${name}`)
			},
			debounceQuery(queryString, callback) {
				this.timer && clearTimeout(this.timer)
				this.timer = setTimeout(() => this.querySearchAsync(queryString, callback), 1000)
			},
			querySearchAsync(queryString, callback) {
				if (queryString == null
						|| queryString.trim() === ''
						|| queryString.indexOf('%') !== -1
						|| queryString.indexOf('_') !== -1
						|| queryString.indexOf('[') !== -1
						|| queryString.indexOf('#') !== -1
						|| queryString.indexOf('*') !== -1
						|| queryString.trim().length > 20) {
					return
				}
				getSearchBlogList(queryString).then(res => {
					if (res.code === 200) {
						this.queryResult = res.data
						if (this.queryResult.length === 0) {
							this.queryResult.push({title: '无相关搜索结果'})
						}
						callback(this.queryResult)
					}
				}).catch(() => {
					this.msgError("请求失败")
				})
			},
			handleSelect(item) {
				if (item.id) {
					this.$router.push(`/blog/${item.id}`)
				}
			}
		}
	}
</script>

<style>
	.ui.fixed.menu .container {
		width: 1180px !important;
		margin-left: auto !important;
		margin-right: auto !important;
	}

	.ui.fixed.menu {
		min-height: 60px;
		border: 0 !important;
		background: rgba(255, 255, 255, .9) !important;
		box-shadow: 0 3px 18px rgba(24, 39, 54, .08) !important;
		backdrop-filter: blur(12px);
		-webkit-backdrop-filter: blur(12px);
		transition: background .3s ease-out, box-shadow .3s ease-out;
	}

	.ui.inverted.pointing.menu.transparent {
		background: linear-gradient(180deg, rgba(0, 0, 0, .24), rgba(0, 0, 0, .04) 72%, rgba(0, 0, 0, 0)) !important;
		box-shadow: none !important;
		backdrop-filter: none;
		-webkit-backdrop-filter: none;
	}

	.ui.inverted.pointing.menu .item,
	.ui.inverted.pointing.menu .header,
	.ui.inverted.pointing.menu .el-dropdown-link,
	.ui.inverted.pointing.menu .m-search input,
	.ui.inverted.pointing.menu .m-search i {
		color: rgba(38, 50, 56, .9) !important;
		text-shadow: none;
	}

	.ui.inverted.pointing.menu.transparent .item,
	.ui.inverted.pointing.menu.transparent .header,
	.ui.inverted.pointing.menu.transparent .el-dropdown-link,
	.ui.inverted.pointing.menu.transparent .m-search input,
	.ui.inverted.pointing.menu.transparent .m-search i {
		color: rgba(255, 255, 255, .95) !important;
		text-shadow: 0 1px 8px rgba(0, 0, 0, .36);
	}

	.ui.inverted.pointing.menu .item,
	.ui.inverted.pointing.menu .el-dropdown-link {
		margin: 0 .35em !important;
		border-radius: 0 !important;
	}

	.ui.inverted.pointing.menu .item:hover,
	.ui.inverted.pointing.menu .active.item,
	.ui.inverted.pointing.menu .active.item:hover,
	.ui.inverted.pointing.menu .el-dropdown-link:hover {
		background: transparent !important;
		color: #48dbfb !important;
	}

	.ui.inverted.pointing.menu .active.item:after {
		display: none !important;
	}

	.ui.inverted.pointing.menu.transparent .active.item:after {
		display: none !important;
	}

	.ui.inverted.pointing.menu.transparent .active.item:hover:after {
		display: none !important;
	}

	.el-dropdown-link {
		outline-style: none !important;
		outline-color: unset !important;
		height: 100%;
		cursor: pointer;
	}

	.el-dropdown-menu {
		margin: 8px 0 0 0 !important;
		padding: 6px !important;
		border: 1px solid rgba(238, 242, 247, .95) !important;
		border-radius: 8px !important;
		background: rgba(255, 255, 255, .96) !important;
		box-shadow: 0 10px 30px rgba(24, 39, 54, .12) !important;
	}

	.el-dropdown-menu__item {
		border-radius: 6px !important;
		padding: 0 15px !important;
		color: rgba(38, 50, 56, .88) !important;
	}

	.el-dropdown-menu__item:hover {
		background: rgba(72, 219, 251, .12) !important;
		color: #00a7e0 !important;
	}

	.el-popper .popper__arrow::after {
		content: none !important;
	}

	.popper__arrow {
		display: none !important;
	}

	.m-search {
		min-width: 220px;
		padding: 0 !important;
	}

	.m-search input {
		border: 0px !important;
		background-color: transparent;
		padding: .67857143em 2.1em .67857143em 1em;
	}

	.m-search input::placeholder {
		color: inherit;
		opacity: .72;
	}

	.m-search-item {
		min-width: 350px !important;
	}

	.m-search-item li {
		line-height: normal !important;
		padding: 8px 10px !important;
	}

	.m-search-item li .title {
		text-overflow: ellipsis;
		overflow: hidden;
		color: rgba(0, 0, 0, 0.87);
	}

	.m-search-item li .content {
		text-overflow: ellipsis;
		font-size: 12px;
		color: rgba(0, 0, 0, .70);
	}
</style>
