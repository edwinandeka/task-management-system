import { User } from './user.model';

export interface TaskComment {
  id: number;
  taskId: number;
  content: string;
  author: User;
  createdAt: string;
}

export interface TaskCommentRequest {
  content: string;
}
