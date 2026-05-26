export interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  category: string;
  photoFileId?: string;
  isAvailable: boolean;
  isHidden: boolean;
  subcategory?: string;
  volume?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateMenuItemRequest {
  name: string;
  description: string;
  price: number;
  category: string;
  photoFileId?: string;
  subcategory?: string;
  volume?: string;
}

export interface UpdateMenuItemRequest extends Partial<CreateMenuItemRequest> {
  isAvailable?: boolean;
  isHidden?: boolean;
}
