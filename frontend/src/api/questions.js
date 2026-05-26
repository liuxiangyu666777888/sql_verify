import http from './index'

export const getQuestion = (params) => http.get('/question', { params })
export const createQuestion = (data) => http.post('/question', data)
export const deleteQuestion = (id, params) => http.delete(`/question/${id}`, { params })
export const getQuestionList = (params) => http.get('/questionlist', { params })
export const getAnswer = (params) => http.get('/answer', { params })
export const createTestCase = (data) => http.post('/testcase', data)
export const checkQuestions = (data) => http.post('/check-questions', data)
