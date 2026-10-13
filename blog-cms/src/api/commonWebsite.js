import axios from '@/util/request'

export function getWebsitesByQuery(queryInfo) {
	return axios({
		url: 'websites',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function updatePublished(id, published) {
	return axios({
		url: 'website/published',
		method: 'PUT',
		params: {
			id,
			published
		}
	})
}

export function saveWebsite(form) {
	return axios({
		url: 'website',
		method: 'POST',
		data: {
			...form
		}
	})
}

export function updateWebsite(form) {
	return axios({
		url: 'website',
		method: 'PUT',
		data: {
			...form
		}
	})
}

export function deleteWebsiteById(id) {
	return axios({
		url: 'website',
		method: 'DELETE',
		params: {
			id
		}
	})
}
