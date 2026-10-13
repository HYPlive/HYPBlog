<template>
	<div class="websites-page">
		<div class="websites-heading">
			<h2>常用网站</h2>
			<p>经常访问的网站集合，点击卡片即可跳转。</p>
		</div>
		<div v-if="websiteList.length" class="website-grid">
			<a v-for="(item, index) in websiteList" :key="index" :href="item.url" target="_blank" rel="external nofollow noopener"
			   class="website-card" :style="{'--accent': item.color || '#48dbfb'}">
				<div class="website-icon">
					<img v-if="isImageUrl(item.icon)" :src="item.icon" alt="">
					<span v-else-if="item.icon">{{ item.icon }}</span>
					<i v-else class="linkify icon"></i>
				</div>
				<div class="website-body">
					<div class="website-name">{{ item.name }}</div>
					<div class="website-desc">{{ item.description }}</div>
				</div>
			</a>
		</div>
		<p v-else class="empty">暂无常用网站</p>
	</div>
</template>

<script>
	import {getWebsiteList} from '@/api/commonWebsite'

	export default {
		name: 'Websites',
		data() {
			return {
				websiteList: []
			}
		},
		created() {
			getWebsiteList().then(res => {
				if (res.code === 200) {
					this.websiteList = res.data
				}
			}).catch(() => this.msgError('常用网站加载失败'))
		},
		methods: {
			isImageUrl(icon) {
				return icon && (icon.startsWith('http') || icon.startsWith('/'))
			}
		}
	}
</script>

<style scoped>
	.websites-heading {
		margin-bottom: 22px;
	}

	.websites-heading h2 {
		margin-bottom: 5px;
	}

	.websites-heading p {
		color: #909399;
		margin: 0;
	}

	.website-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
		gap: 16px;
	}

	.website-card {
		--accent: #48dbfb;
		display: flex;
		align-items: center;
		gap: 12px;
		padding: 16px;
		background: rgba(255, 255, 255, .9);
		border: 1px solid #edf0f5;
		border-top: 3px solid var(--accent);
		border-radius: 8px;
		transition: transform .2s ease, box-shadow .2s ease;
	}

	.website-card:hover {
		transform: translateY(-3px);
		box-shadow: 0 8px 20px rgba(24, 39, 54, .12);
	}

	.website-icon {
		flex: 0 0 auto;
		width: 44px;
		height: 44px;
		border-radius: 10px;
		background: var(--accent);
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		font-size: 22px;
		line-height: 1;
	}

	.website-icon img {
		width: 26px;
		height: 26px;
		border-radius: 4px;
		object-fit: contain;
	}

	.website-icon .icon {
		margin: 0;
	}

	.website-body {
		min-width: 0;
	}

	.website-name {
		font-size: 15px;
		font-weight: 600;
		color: var(--accent);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.website-desc {
		margin-top: 3px;
		font-size: 12px;
		color: #909399;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.empty {
		padding: 60px 15px;
		text-align: center;
		color: #c0c4cc;
	}

	@media (max-width: 768px) {
		.website-grid {
			grid-template-columns: repeat(2, 1fr);
		}
	}
</style>
