export interface Task {
  id: number;
  title: string;
  description: string;
  assignedTo?: number;
  assignedToName?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
}

export interface CreateTaskRequest {
  title: string;
  description: string;
  assignedTo?: number;
  priority: Task['priority'];
  dueDate?: string;
}

export interface UpdateTaskRequest extends Partial<CreateTaskRequest> {
  status?: Task['status'];
}
