import http from './index'

export const getContest = (params) => http.get('/contest', { params })
export const createContest = (data) => http.post('/contest', data)
export const deleteContest = (id) => http.delete(`/contest/${id}`)
export const getContestList = (params) => http.get('/contestlist', { params })
export const getContestQuestions = (params) => http.get('/contest-question', { params })
export const addContestQuestion = (data) => http.post('/contest-question', data)
export const addContestStudent = (data) => http.post('/contest-student', data)
export const getContestStudent = (params) => http.get('/contest-student', { params })
export const getContestScores = (params) => http.get('/contestscores', { params })
export const getScore = (params) => http.get('/getscore', { params })
export const updateScore = (data) => http.post('/updatescore', data)
