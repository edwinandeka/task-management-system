import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { TaskComment, TaskCommentRequest } from '../models/task-comment.model';
import { TaskHistory } from '../models/task-history.model';
import { Task, TaskRequest, TaskStatusUpdate } from '../models/task.model';
import { API_BASE_URL } from './api.config';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);

  list(): Observable<Task[]> {
    return this.http.get<Task[]>(`${API_BASE_URL}/tasks`);
  }

  getById(id: number): Observable<Task> {
    return this.http.get<Task>(`${API_BASE_URL}/tasks/${id}`);
  }

  create(request: TaskRequest): Observable<Task> {
    return this.http.post<Task>(`${API_BASE_URL}/tasks`, request);
  }

  update(id: number, request: TaskRequest): Observable<Task> {
    return this.http.put<Task>(`${API_BASE_URL}/tasks/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/tasks/${id}`);
  }

  updateStatus(id: number, status: TaskStatusUpdate): Observable<Task> {
    return this.http.patch<Task>(`${API_BASE_URL}/tasks/${id}/status`, status);
  }

  listComments(taskId: number): Observable<TaskComment[]> {
    return this.http.get<TaskComment[]>(`${API_BASE_URL}/tasks/${taskId}/comments`);
  }

  createComment(taskId: number, request: TaskCommentRequest): Observable<TaskComment> {
    return this.http.post<TaskComment>(`${API_BASE_URL}/tasks/${taskId}/comments`, request);
  }

  listHistory(taskId: number): Observable<TaskHistory[]> {
    return this.http.get<TaskHistory[]>(`${API_BASE_URL}/tasks/${taskId}/history`);
  }
}
