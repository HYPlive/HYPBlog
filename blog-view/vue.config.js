function ignoreGoogleFontImports(config) {
	const types = ['vue-modules', 'vue', 'normal-modules', 'normal']
	types.forEach(type => {
		config.module
			.rule('css')
			.oneOf(type)
			.use('postcss-loader')
			.tap(options => ({
				...options,
				plugins: [
					...(options.plugins || []),
					root => {
						root.walkAtRules('import', rule => {
							if (rule.params.includes('fonts.googleapis.com')) {
								rule.remove()
							}
						})
					}
				]
			}))
	})
}

module.exports = {
	chainWebpack: ignoreGoogleFontImports,
	configureWebpack: {
		resolve: {
			alias: {
				'assets': '@/assets',
				'common': '@/common',
				'components': '@/components',
				'api': '@/api',
				'views': '@/views',
				'plugins': '@/plugins'
			}
		},

		optimization: {
			splitChunks: {
				cacheGroups: {
					vendor: {
						test: /[\\/]node_modules[\\/]/,
						name(module) {
							const packageName = module.context.match(
								/[\\/]node_modules[\\/](.*?)([\\/]|$)/
							)[1];

							return `npm.${packageName.replace("@", "")}`;
						},
						chunks: "all",
						enforce: true,
						priority: 10,
						minSize: 50000,
						maxSize: 200000,
						reuseExistingChunk: true,
					},
				},
			},
		},
	},
}
