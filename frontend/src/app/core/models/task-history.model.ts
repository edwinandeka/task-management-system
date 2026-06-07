import { TaskStatus } from './task.model';
import { User } from './user.model';

export interface TaskHistory {
  id: number;
  taskId: number;
  fromStatus: TaskStatus | null;
  toStatus: TaskStatus;
  changedBy: User;
  changedAt: string;
}
