<template>
	<header ref="header" :style="{backgroundImage: `url(${headerImage})`}">
		<div class="hitokoto">
			<p class="sentence">{{ hitokoto.hitokoto || 'HYP\'s Blog' }}</p>
			<p class="from" v-if="hitokoto.from">——《{{ hitokoto.from }}》</p>
		</div>
		<div class="wrapper">
			<i class="ali-iconfont icon-down" @click="scrollToMain"></i>
		</div>
	</header>
</template>

<script>
	import {mapState} from 'vuex'

	export default {
		name: "Header",
		props: {
			hitokoto: {
				type: Object,
				default: () => ({})
			},
			backgroundImage: {
				type: String,
				default: ''
			}
		},
		computed: {
			...mapState(['clientSize']),
			headerImage() {
				return this.backgroundImage || '/img/header/hitokoto-bg.jpg.jpeg'
			}
		},
		watch: {
			'clientSize.clientHeight'() {
				this.setHeaderHeight()
			}
		},
		mounted() {
			this.setHeaderHeight()
		},
		methods: {
			setHeaderHeight() {
				this.$refs.header.style.height = this.clientSize.clientHeight + 'px'
			},
			scrollToMain() {
				window.scrollTo({top: this.clientSize.clientHeight, behavior: 'smooth'})
			}
		},
	}
</script>

<style scoped>
	header {
		position: relative;
		user-select: none;
		background-position: center center;
		background-size: cover;
		background-repeat: no-repeat;
		background-attachment: fixed;
		overflow: hidden;
	}

	.hitokoto {
		position: absolute;
		top: 48%;
		left: 50%;
		width: min(900px, 82vw);
		transform: translate(-50%, -50%);
		color: #fff;
		text-align: center;
		z-index: 50;
	}

	.sentence {
		margin: 0;
		font-size: 40px;
		font-weight: 500;
		line-height: 1.65;
		letter-spacing: 0;
	}

	.from {
		margin-top: 24px;
		font-size: 20px;
		line-height: 1.5;
		opacity: .88;
	}

	.wrapper {
		position: absolute;
		width: 100px;
		bottom: 150px;
		left: 0;
		right: 0;
		margin: auto;
		font-size: 26px;
		z-index: 100;
	}

	.wrapper i {
		font-size: 60px;
		color: #fff;
		opacity: 0.75;
		cursor: pointer;
		position: absolute;
		top: 55px;
		left: 20px;
		animation: opener .5s ease-in-out alternate infinite;
		transition: opacity .2s ease-in-out, transform .5s ease-in-out .2s;
		text-shadow: 0 2px 12px rgba(0, 0, 0, .5);
	}

	.wrapper i:hover {
		opacity: 1;
	}

	@keyframes opener {
		100% {
			top: 65px
		}
	}

	@media (max-width: 768px) {
		header {
			background-attachment: scroll;
		}

		.sentence {
			font-size: 28px;
		}

		.from {
			font-size: 16px;
		}
	}
</style>
