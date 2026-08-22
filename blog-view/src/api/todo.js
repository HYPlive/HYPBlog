import axios from '@/plugins/axios'

export function getTodoList() {
	return axios({url: 'todos', method: 'GET'})
}
