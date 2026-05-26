import http from './index'

export const getUsers = () => http.get('/manageusers')
export const updateUserRole = (data) => http.put('/manageusers', data)
export const deleteUser = (params) => http.delete('/manageusers', { params })
export const getStudent = (params) => http.get('/student', { params })
export const getStudentList = () => http.get('/studentlist')
export const updateSettings = (data) => http.post('/updateSettings', data)
export const getAssistantStudents = (params) => http.get('/assistantstudents', { params })
export const assignAssistant = (data) => http.post('/assistantstudents', data)
export const getDashboard = (params) => http.get('/teacher-dashboard', { params })
