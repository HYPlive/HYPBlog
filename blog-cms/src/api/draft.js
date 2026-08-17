import axios from '@/util/request'

export function getDraftDataByQuery(queryInfo) {
	return axios({
		url: 'drafts',
		method: 'GET',
		params: {
			...queryInfo
		}
	})
}

export function deleteDraftById(id) {
	return axios({
		url: 'draft',
		method: 'DELETE',
		params: {
			id
		}
	})
}

export function saveDraft(blog) {
	return axios({
		url: 'draft',
		method: 'POST',
		data: {
			...blog
		}
	})
}

export function getDraftById(id) {
	return axios({
		url: 'draft',
		method: 'GET',
		params: {
			id
		}
	})
}

export function updateDraft(blog) {
	return axios({
		url: 'draft',
		method: 'PUT',
		data: {
			...blog
		}
	})
}
