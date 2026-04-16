import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MenuItem, CreateMenuItemRequest } from '../../interfaces';
import { MenuService } from '../../services';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent implements OnInit {
  isUploadingPhoto = false;
  menuItems: MenuItem[] = [];
  filteredItems: MenuItem[] = [];
  searchTerm = '';
  selectedCategory = '';
  categories = ['COFFEE', 'TEA', 'DESSERT', 'SANDWICH', 'SNACK', 'OTHER'];
  isLoading = false;

  isCreateModalOpen = false;
  isEditModalOpen = false;
  selectedItem: MenuItem | null = null;
  createErrors: { name?: string; price?: string } = {};
  editErrors: { name?: string; price?: string } = {};

  newItem: CreateMenuItemRequest = {
    name: '',
    description: '',
    price: 0,
    category: 'COFFEE',
    subcategory: '',
    photoFileId: ''
  };

  constructor(
    private menuService: MenuService,
    private authService: AuthService,
    private router: Router
  ) {}

  get isAdmin(): boolean {
    return this.authService.isAdmin;
  }

  get isStaff(): boolean {
    return this.authService.isStaff;
  }

  ngOnInit(): void {
    this.loadMenuItems();
  }

  loadMenuItems(): void {
    this.isLoading = true;
    this.menuService.getAllMenuItems().subscribe({
      next: (items) => {
        this.menuItems = items;
        this.filteredItems = items;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading menu items:', error);
        this.isLoading = false;
      }
    });
  }

  filterItems(): void {
    this.filteredItems = this.menuItems.filter(item => {
      const matchesSearch = item.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                            item.description.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesCategory = !this.selectedCategory || item.category === this.selectedCategory;
      return matchesSearch && matchesCategory;
    });
  }

  openCreateModal(): void {
    this.isCreateModalOpen = true;
    this.createErrors = {};
    this.newItem = { name: '', description: '', price: 0, category: 'COFFEE', subcategory: '', photoFileId: '' };
  }

  closeCreateModal(): void { this.isCreateModalOpen = false; }

  openEditModal(item: MenuItem): void {
    this.selectedItem = { ...item };
    this.isEditModalOpen = true;
  }

  closeEditModal(): void {
    this.isEditModalOpen = false;
    this.selectedItem = null;
    this.editErrors = {};
  }

  createItem(): void {
    this.createErrors = {};
    if (!this.newItem.name?.trim()) this.createErrors.name = 'Введите название блюда';
    if (!this.newItem.price || this.newItem.price <= 0) this.createErrors.price = 'Цена должна быть больше 0';
    if (this.createErrors.name || this.createErrors.price) return;

    const userId = this.authService.currentUser?.id ?? 1;
    // Закрываем модалку ДО запроса, чтобы не было двойного нажатия
    this.closeCreateModal();
    this.menuService.createMenuItem(this.newItem, userId).subscribe({
      next: () => { this.loadMenuItems(); },
      error: (error) => console.error('Error creating menu item:', error)
    });
  }

  updateItem(): void {
    if (!this.selectedItem) return;
    this.editErrors = {};
    if (!this.selectedItem.name?.trim()) this.editErrors.name = 'Введите название блюда';
    if (!this.selectedItem.price || this.selectedItem.price <= 0) this.editErrors.price = 'Цена должна быть больше 0';
    if (this.editErrors.name || this.editErrors.price) return;

    const item = { ...this.selectedItem };
    // Закрываем модалку ДО запроса
    this.closeEditModal();
    this.menuService.updateMenuItem(item.id, item).subscribe({
      next: () => { this.loadMenuItems(); },
      error: (error) => console.error('Error updating menu item:', error)
    });
  }

  deleteItem(item: MenuItem): void {
    if (confirm(`Удалить "${item.name}"?`)) {
      this.menuService.deleteMenuItem(item.id).subscribe({
        next: () => this.loadMenuItems(),
        error: (error) => console.error('Error deleting menu item:', error)
      });
    }
  }

  toggleAvailability(item: MenuItem): void {
    this.menuService.toggleAvailability(item.id, !item.isAvailable).subscribe({
      next: () => { item.isAvailable = !item.isAvailable; },
      error: (error) => console.error('Error toggling availability:', error)
    });
  }

  toggleVisibility(item: MenuItem): void {
    this.menuService.toggleVisibility(item.id, !item.isHidden).subscribe({
      next: () => { item.isHidden = !item.isHidden; },
      error: (error) => console.error('Error toggling visibility:', error)
    });
  }

  getCategoryDisplayName(category: string): string {
    const names: { [key: string]: string } = {
      'COFFEE': 'Кофе', 'TEA': 'Чай', 'DESSERT': 'Десерты',
      'SANDWICH': 'Сэндвичи', 'SNACK': 'Закуски', 'OTHER': 'Другое'
    };
    return names[category] || category;
  }

  goBack(): void { this.router.navigate(['/dashboard']); }

  onPhotoSelected(event: Event, isEdit: boolean = false): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.isUploadingPhoto = true;
    this.menuService.uploadPhoto(input.files[0]).subscribe({
      next: (filename: string) => {
        const photoUrl = `${environment.apiUrl}/uploads/${filename}`;
        if (isEdit && this.selectedItem) {
          this.selectedItem.photoFileId = photoUrl;
        } else {
          this.newItem.photoFileId = photoUrl;
        }
        this.isUploadingPhoto = false;
      },
      error: (error) => {
        console.error('Error uploading photo:', error);
        this.isUploadingPhoto = false;
      }
    });
  }
}
