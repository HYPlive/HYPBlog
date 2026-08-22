<template>
	<footer class="ui inverted vertical segment m-padded-tb-large site-footer">
		<div class="ui center aligned container">
			<div class="ui inverted divided stackable grid">

				<div class="three wide column">
					<div class="ui link list">
						<h4 class="ui inverted header m-text-thin m-text-spaced">{{ siteInfo.footerImgTitle }}</h4>
						<div class="item">
							<img :src="siteInfo.footerImgUrl" class="ui rounded image footer-qr" alt="">
						</div>
					</div>
				</div>

				<div class="six wide column">
					<h4 class="ui inverted header m-text-thin m-text-spaced">最新博客</h4>
					<div class="ui inverted link list">
						<a href="javascript:;" @click.prevent="toBlog(item)" v-for="item in newBlogList" :key="item.id" class="item m-text-thin m-padded-tb-small">{{ item.title }}</a>
					</div>
				</div>

				<div class="seven wide column">
					<p id="hitokotoText" class="m-text-thin m-text-spaced m-opacity-mini">{{ hitokoto.hitokoto }}</p>
					<p id="hitokotoFrom" class="m-text-thin m-text-spaced m-opacity-mini" style="float: right" v-text="hitokoto.from?`——《${hitokoto.from}》`:''"></p>
				</div>
			</div>

			<div class="ui inverted section divider footer-divider"></div>

			<p class="m-text-thin m-text-spaced footer-copyright">
				<span style="margin-right: 10px" v-if="siteInfo.copyright">{{ siteInfo.copyright.title }}</span>
				<router-link to="/" class="footer-link" v-if="siteInfo.copyright">{{ siteInfo.copyright.siteName }}</router-link>
				<span style="margin: 0 15px" v-if="siteInfo.copyright && siteInfo.beian">|</span>
				<a rel="external nofollow noopener" href="https://beian.miit.gov.cn/" target="_blank" class="footer-link">{{ siteInfo.beian }}</a>
				<span style="margin: 0 15px" v-if="siteInfo.policeBeian">|</span>
				<a :href="policeBeianUrl" rel="noreferrer" target="_blank" class="footer-link">{{ siteInfo.policeBeian }}</a>
			</p>

			<div class="github-badge" v-for="(item,index) in badges" :key="index">
				<a rel="external nofollow noopener" :href="item.url" target="_blank" :title="item.title">
					<span class="badge-subject">{{ item.subject }}</span>
					<span class="badge-value" :class="`bg-${item.color}`">{{ item.value }}</span>
				</a>
			</div>

		</div>
	</footer>
</template>

<script>
	export default {
		name: "Footer",
		props: {
			siteInfo: {
				type: Object,
				required: true
			},
			badges: {
				type: Array,
				required: true
			},
			newBlogList: {
				type: Array,
				required: true
			},
			hitokoto: {
				type: Object,
				required: true
			}
		},
		computed: {
			policeBeianUrl() {
				const code = (this.siteInfo.policeBeian || '').replace(/\D/g, '')
				return `https://beian.mps.gov.cn/#/query/webSearch?code=${code}`
			}
		},
		methods: {
			toBlog(blog) {
				this.$store.dispatch('goBlogPage', blog)
			}
		}
	}
</script>

<style scoped>
	@import "../../assets/css/badge.css";

	.site-footer.ui.inverted.segment {
		position: relative;
		overflow: hidden;
		margin-top: 2.5em !important;
		border: 0 !important;
		border-top: 1px solid rgba(255, 255, 255, .18) !important;
		background: linear-gradient(180deg, rgba(18, 24, 32, .72), rgba(18, 24, 32, .9)) !important;
		box-shadow: 0 -12px 36px rgba(18, 24, 32, .18) !important;
		backdrop-filter: blur(12px) saturate(130%);
		-webkit-backdrop-filter: blur(12px) saturate(130%);
	}

	.site-footer.ui.inverted.segment::before {
		content: "";
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 1px;
		background: linear-gradient(90deg, transparent, rgba(72, 219, 251, .65), transparent);
	}

	.site-footer >>> .ui.inverted.divided.grid > .column:not(.row),
	.site-footer >>> .ui.inverted.divided.grid > .row > .column {
		box-shadow: -1px 0 0 0 rgba(255, 255, 255, .12);
	}

	.site-footer >>> .ui.inverted.header {
		color: rgba(255, 255, 255, .92) !important;
	}

	.site-footer >>> .ui.inverted.link.list .item,
	.site-footer p {
		color: rgba(255, 255, 255, .72) !important;
	}

	.site-footer >>> .ui.inverted.link.list .item:hover,
	.footer-link:hover {
		color: #48dbfb !important;
	}

	.footer-qr {
		width: 100px;
		padding: 8px;
		background: rgba(255, 255, 255, .88);
		box-shadow: 0 10px 30px rgba(0, 0, 0, .18);
	}

	.footer-divider {
		border-top-color: rgba(255, 255, 255, .14) !important;
		border-bottom-color: rgba(255, 255, 255, .04) !important;
	}

	.footer-copyright {
		opacity: .78;
	}

	.footer-link {
		color: #48dbfb !important;
		transition: color .2s ease;
	}

	.github-badge a {
		color: #fff;
	}
</style>
