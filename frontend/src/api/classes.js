import http from './index'

export const getClass = (params) => http.get('/class', { params })
export const createClass = (data) => http.post('/class', data)
export const updateClass = (data) => http.put('/class', data)
export const deleteClass = (params) => http.delete('/class', { params })
export const getClassList = (params) => http.get('/classlist', { params })
export const getClassStudents = (params) => http.get('/class-student', { params })
export const addClassStudents = (data) => http.post('/class-student', data)
export const removeClassStudent = (params) => http.delete('/class-student', { params })
