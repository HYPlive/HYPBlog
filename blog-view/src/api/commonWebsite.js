import axios from '@/plugins/axios'

export function getWebsiteList() {
	return axios({
		url: 'websites',
		method: 'GET'
	})
}
