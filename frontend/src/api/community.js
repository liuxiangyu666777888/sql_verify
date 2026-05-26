import http from './index'

export const getArticle = (params) => http.get('/community', { params })
export const createArticle = (data) => http.post('/community', data)
export const updateArticle = (data) => http.put('/community', data)
export const deleteArticle = (params) => http.delete('/community', { params })
export const getCommunityList = (params) => http.get('/communitylist', { params })
