import http from './index'

export const submit = (data) => http.post('/submit', data)
export const getSubmit = (params) => http.get('/submit', { params })
export const deleteSubmit = (params) => http.delete('/submit', { params })
export const getSubmitList = (params) => http.get('/submitlist', { params })
export const judge = (data) => http.post('/judge', data)
export const getStatusCount = (params) => http.get('/statuscount', { params })
export const getAnsweredQuestions = (params) => http.get('/answeredquestions', { params })
