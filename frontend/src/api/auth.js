import http from './index'

export const login = (data) => http.post('/login', data)
export const logout = () => http.delete('/login')
export const getSession = () => http.get('/login')
export const register = (data) => http.post('/register', data)
