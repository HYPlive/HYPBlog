<template>
	<div class="contribution-calendar">
		<div class="ui segments m-box">
			<div class="ui segment">
				<!--月历-->
				<div class="cc-calendar">
					<div class="cc-calendar-header">
						<i class="angle left icon" @click="changeMonth(-1)"></i>
						<span>{{ currentYear }}年{{ currentMonth + 1 }}月</span>
						<i class="angle right icon" @click="changeMonth(1)"></i>
					</div>
					<div class="cc-weekday-row">
						<span v-for="w in weekdays" :key="w">{{ w }}</span>
					</div>
					<div class="cc-calendar-grid">
						<span v-for="cell in calendarCells" :key="cell.key"
						      class="cc-day"
						      :class="{empty: cell.empty}"
						      @click="selectDay(cell)">
							<span class="num" :class="{today: cell.isToday, selected: !cell.empty && cell.key === selectedDate}">{{ cell.day || '' }}</span>
							<span v-if="!cell.empty && cell.count > 0" class="dot"></span>
						</span>
					</div>
				</div>

				<!--热力图：12列(月)×4行(周)-->
				<div class="cc-heatmap">
					<div class="cc-months">
						<div v-for="col in heatmapWeeks" :key="col.month" class="cc-month">
							<span v-for="week in col.weeks" :key="week.key"
							      class="cc-cell"
							      :class="[week.future ? 'future' : 'level-' + week.level, {selected: week.key === selectedWeekKey}]"
							      @mouseenter="showTooltip(week, $event)"
							      @mouseleave="hideTooltip"
							      @click="selectWeek(week)"></span>
							<span class="cc-month-label">{{ col.month }}</span>
						</div>
					</div>
					<div class="cc-heatmap-legend">
						<span class="cc-legend-text">少</span>
						<span class="cc-cell level-0"></span>
						<span class="cc-cell level-1"></span>
						<span class="cc-cell level-2"></span>
						<span class="cc-cell level-3"></span>
						<span class="cc-cell level-4"></span>
						<span class="cc-legend-text">多</span>
					</div>
				</div>

				<!--文章列表-->
				<div class="cc-list">
					<div class="cc-list-header">{{ listTitle || '提交日历' }}</div>
					<ul v-if="selectedArticles.length">
						<li v-for="item in selectedArticles" :key="item.id">
							<router-link :to="'/blog/' + item.id">{{ item.title }}</router-link>
						</li>
					</ul>
					<p v-else class="cc-list-empty">{{ listTitle ? '该时间段暂无文章' : '点击日期或热力图查看当天/当周文章' }}</p>
				</div>
			</div>
		</div>

		<!--自定义 tooltip：位于鼠标右下方-->
		<div class="cc-tooltip" v-show="tooltip.show" :style="{left: tooltip.x + 'px', top: tooltip.y + 'px'}">{{ tooltip.text }}</div>
	</div>
</template>

<script>
	import {getContribution} from '@/api/blog'

	export default {
		name: 'ContributionCalendar',
		data() {
			const now = new Date()
			return {
				contributionList: [],
				currentYear: now.getFullYear(),
				currentMonth: now.getMonth(),
				weekdays: ['一', '二', '三', '四', '五', '六', '日'],
				selectedDate: '',
				selectedWeekKey: '',
				listTitle: '',
				selectedArticles: [],
				tooltip: {show: false, text: '', x: 0, y: 0}
			}
		},
		computed: {
			countByDate() {
				const map = {}
				this.contributionList.forEach(item => {
					map[item.date] = (map[item.date] || 0) + 1
				})
				return map
			},
			articlesByDate() {
				const map = {}
				this.contributionList.forEach(item => {
					if (!map[item.date]) {
						map[item.date] = []
					}
					map[item.date].push(item)
				})
				return map
			},
			weekMap() {
				const map = {}
				this.contributionList.forEach(item => {
					const key = this.weekKeyFromDateStr(item.date)
					if (!map[key]) {
						map[key] = {count: 0, articles: [], label: this.weekLabelFromKey(key)}
					}
					map[key].count++
					map[key].articles.push(item)
				})
				return map
			},
			calendarCells() {
				const year = this.currentYear
				const month = this.currentMonth
				const offset = (new Date(year, month, 1).getDay() + 6) % 7
				const daysInMonth = new Date(year, month + 1, 0).getDate()
				const now = new Date()
				const cells = []
				for (let i = 0; i < offset; i++) {
					cells.push({empty: true, key: 'empty-' + i})
				}
				for (let d = 1; d <= daysInMonth; d++) {
					const key = this.dateStr(year, month, d)
					cells.push({
						empty: false,
						day: d,
						key: key,
						count: this.countByDate[key] || 0,
						isToday: year === now.getFullYear() && month === now.getMonth() && d === now.getDate()
					})
				}
				return cells
			},
			//当前年份的 12 个月 × 4 周，未来月份也占位显示
			heatmapWeeks() {
				const year = new Date().getFullYear()
				const today = new Date()
				today.setHours(0, 0, 0, 0)
				const columns = []
				for (let m = 1; m <= 12; m++) {
					const weeks = []
					for (let w = 1; w <= 4; w++) {
						const key = year + '-' + m + '-' + w
						const wk = this.weekMap[key]
						const count = wk ? wk.count : 0
						const firstDay = new Date(year, m - 1, (w - 1) * 7 + 1)
						const future = firstDay.getTime() > today.getTime()
						weeks.push({
							key: key,
							label: m + '月第' + w + '周',
							count: count,
							future: future,
							level: this.levelOf(count)
						})
					}
					columns.push({month: m, weeks: weeks})
				}
				return columns
			}
		},
		created() {
			getContribution().then(res => {
				if (res.code === 200) {
					this.contributionList = res.data || []
				}
			}).catch(() => this.msgError('提交日历加载失败'))
		},
		methods: {
			pad(n) {
				return n < 10 ? '0' + n : '' + n
			},
			dateStr(y, m, d) {
				return y + '-' + this.pad(m + 1) + '-' + this.pad(d)
			},
			levelOf(count) {
				if (count <= 0) return 0
				if (count === 1) return 1
				if (count === 2) return 2
				if (count === 3) return 3
				return 4
			},
			weekIndex(d) {
				return Math.min(Math.floor((d - 1) / 7) + 1, 4)
			},
			weekKeyFromDateStr(dateStr) {
				const parts = dateStr.split('-').map(Number)
				return parts[0] + '-' + parts[1] + '-' + this.weekIndex(parts[2])
			},
			weekLabelFromKey(key) {
				const parts = key.split('-').map(Number)
				return parts[1] + '月第' + parts[2] + '周'
			},
			dateLabel(dateStr) {
				const parts = dateStr.split('-').map(Number)
				return parts[0] + '年' + parts[1] + '月' + parts[2] + '日'
			},
			changeMonth(delta) {
				let y = this.currentYear
				let m = this.currentMonth + delta
				if (m < 0) {
					m = 11
					y--
				}
				if (m > 11) {
					m = 0
					y++
				}
				this.currentYear = y
				this.currentMonth = m
			},
			selectDay(cell) {
				if (cell.empty) return
				this.selectedDate = cell.key
				this.selectedWeekKey = ''
				this.listTitle = this.dateLabel(cell.key)
				this.selectedArticles = this.articlesByDate[cell.key] || []
			},
			selectWeek(week) {
				if (week.future) return
				this.selectedWeekKey = week.key
				this.selectedDate = ''
				this.listTitle = week.label
				const wk = this.weekMap[week.key]
				this.selectedArticles = wk ? wk.articles : []
			},
			showTooltip(week, e) {
				if (week.future) return
				const rect = this.$el.getBoundingClientRect()
				this.tooltip.show = true
				this.tooltip.text = week.label + '，' + week.count + '篇文章'
				this.tooltip.x = e.clientX - rect.left
				this.tooltip.y = e.clientY - rect.top
			},
			hideTooltip() {
				this.tooltip.show = false
			}
		}
	}
</script>

<style scoped>
	.contribution-calendar {
		width: 100%;
		position: relative;
	}

	.contribution-calendar .ui.segment {
		padding: 14px 10px;
	}

	/*月历*/
	.cc-calendar-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 14px;
		font-weight: 600;
		color: rgba(38, 50, 56, .85);
		margin-bottom: 10px;
	}

	.cc-calendar-header i {
		cursor: pointer;
		color: #909399;
		padding: 2px;
		transition: color .2s;
	}

	.cc-calendar-header i:hover {
		color: #48dbfb;
	}

	.cc-weekday-row {
		display: grid;
		grid-template-columns: repeat(7, 1fr);
		text-align: center;
		font-size: 12px;
		color: #909399;
		margin-bottom: 2px;
	}

	.cc-calendar-grid {
		display: grid;
		grid-template-columns: repeat(7, 1fr);
		row-gap: 2px;
	}

	.cc-day {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: flex-start;
		height: 30px;
		padding-top: 2px;
		cursor: pointer;
		border-radius: 4px;
	}

	.cc-day.empty {
		cursor: default;
	}

	.cc-day .num {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 20px;
		height: 20px;
		font-size: 12px;
		color: rgba(38, 50, 56, .8);
		border-radius: 50%;
	}

	.cc-day .num.today {
		font-weight: 700;
		color: #0f8fc0;
	}

	.cc-day .num.selected {
		background: #eceff3;
	}

	.cc-day:hover .num {
		background: rgba(72, 219, 251, .15);
	}

	.cc-day .dot {
		width: 4px;
		height: 4px;
		border-radius: 50%;
		background: #48dbfb;
		margin-top: 2px;
	}

	/*热力图：12列(月)×4行(周)*/
	.cc-heatmap {
		margin-top: 14px;
	}

	.cc-months {
		display: flex;
		justify-content: space-between;
	}

	.cc-month {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 5px;
	}

	.cc-cell {
		display: inline-block;
		width: 18px;
		height: 18px;
		border-radius: 5px;
		cursor: pointer;
		transition: transform .12s ease, box-shadow .12s ease;
	}

	.cc-month .cc-cell:hover {
		transform: scale(1.18);
	}

	.cc-cell.level-0 {
		background: #ebedf0;
	}

	.cc-cell.level-1 {
		background: #bdeefb;
	}

	.cc-cell.level-2 {
		background: #74daf8;
	}

	.cc-cell.level-3 {
		background: #2bb8e4;
	}

	.cc-cell.level-4 {
		background: #0f8fc0;
	}

	.cc-cell.future {
		background: #f2f4f6;
		cursor: default;
	}

	.cc-cell.selected {
		outline: 1px solid #0f8fc0;
		outline-offset: 0;
	}

	.cc-month-label {
		font-size: 10px;
		color: #909399;
		line-height: 1;
	}

	.cc-heatmap-legend {
		display: flex;
		align-items: center;
		justify-content: flex-end;
		gap: 3px;
		margin-top: 8px;
	}

	.cc-heatmap-legend .cc-cell {
		width: 14px;
		height: 14px;
		cursor: default;
	}

	.cc-legend-text {
		font-size: 11px;
		color: #909399;
	}

	/*文章列表*/
	.cc-list {
		margin-top: 12px;
		border-top: 1px solid #f0f2f5;
		padding-top: 10px;
	}

	.cc-list-header {
		font-size: 13px;
		font-weight: 600;
		color: rgba(38, 50, 56, .85);
		margin-bottom: 6px;
	}

	.cc-list ul {
		margin: 0;
		padding: 0;
		list-style: none;
		max-height: 160px;
		overflow-y: auto;
	}

	.cc-list li {
		padding: 4px 0;
	}

	.cc-list a {
		font-size: 12px;
		color: rgba(38, 50, 56, .75);
		text-decoration: none;
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		transition: color .2s;
	}

	.cc-list a:hover {
		color: #48dbfb;
	}

	.cc-list-empty {
		font-size: 12px;
		color: #c0c4cc;
		margin: 0;
		text-align: center;
		padding: 8px 0;
	}

	/*tooltip*/
	.cc-tooltip {
		position: absolute;
		z-index: 9999;
		padding: 4px 8px;
		border-radius: 4px;
		background: rgba(38, 50, 56, .9);
		color: #fff;
		font-size: 12px;
		pointer-events: none;
		white-space: nowrap;
		transform: translate(-12px, calc(-100% - 12px));
	}
</style>
