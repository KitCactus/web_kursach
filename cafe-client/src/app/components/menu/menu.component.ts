import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MenuItem, CreateMenuItemRequest } from '../../interfaces';
import { MenuService } from '../../services';
import { AuthService } from '../../services/auth.service';
import { PricePipe } from '../../pipes/price.pipe';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, PricePipe],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent implements OnInit {
  isUploadingPhoto = false;
  menuItems: MenuItem[] = [];
  filteredItems: MenuItem[] = [];
  searchTerm = '';
  selectedCategory = '';
  selectedSubcategory = '';
  categories: string[] = [];
  subcategories: string[] = [];
  filteredSubcategories: string[] = [];
  isLoading = false;
  errorMessage: string | null = null;

  isCreateModalOpen = false;
  isEditModalOpen = false;
  selectedItem: MenuItem | null = null;
  createErrors: { name?: string; price?: string; subcategory?: string; description?: string; volume?: string } = {};
  editErrors: { name?: string; price?: string; subcategory?: string; description?: string; volume?: string } = {};

  pendingPhotoFile: File | null = null;
  pendingPhotoPreview: string | null = null;

  newItem: CreateMenuItemRequest = {
    name: '',
    description: '',
    price: 0,
    category: 'COFFEE',
    subcategory: '',
    volume: '',
    photoFileId: ''
  };

  constructor(
    private menuService: MenuService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  get isAdmin(): boolean {
    return this.authService.isAdmin;
  }

  get isStaff(): boolean {
    return this.authService.isStaff;
  }

  getPhotoUrl(photoFileId: string): string {
    if (!photoFileId) return '';
    return `${environment.apiUrl}/images/${photoFileId}`;
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadSubcategories();
    this.loadMenuItems();
  }

  loadCategories(): void {
    this.menuService.getCategories().subscribe({
      next: (cats) => this.categories = cats,
      error: (error) => console.error('Error loading categories:', error)
    });
  }

  loadSubcategories(): void {
    this.menuService.getSubcategories().subscribe({
      next: (subs) => {
        this.subcategories = subs;
        this.filteredSubcategories = subs;
      },
      error: (error) => console.error('Error loading subcategories:', error)
    });
  }

  loadMenuItems(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.menuService.getAllMenuItems().subscribe({
      next: (items) => {
        this.menuItems = items;
        this.filteredItems = items;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading menu items:', error);
        this.errorMessage = 'Не удалось загрузить меню. Проверьте подключение к серверу.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  filterItems(): void {
    this.filteredItems = this.menuItems.filter(item => {
      const matchesSearch = item.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                            item.description.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesCategory = !this.selectedCategory || item.category === this.selectedCategory;
      const matchesSubcategory = !this.selectedSubcategory || item.subcategory === this.selectedSubcategory;
      return matchesSearch && matchesCategory && matchesSubcategory;
    });
  }

  onCategoryChange(): void {
    this.selectedSubcategory = '';
    if (this.selectedCategory) {
      this.menuService.getSubcategoriesByCategory(this.selectedCategory).subscribe({
        next: (subs) => this.filteredSubcategories = subs,
        error: () => this.filteredSubcategories = this.subcategories
      });
    } else {
      this.filteredSubcategories = this.subcategories;
    }
    this.filterItems();
  }

  openCreateModal(): void {
    this.isCreateModalOpen = true;
    this.createErrors = {};
    this.newItem = { name: '', description: '', price: 0, category: this.categories[0] || '', subcategory: '', volume: '', photoFileId: '' };
  }

  closeCreateModal(): void {
    this.isCreateModalOpen = false;
    this.pendingPhotoFile = null;
    this.pendingPhotoPreview = null;
  }

  openEditModal(item: MenuItem): void {
    this.selectedItem = { ...item };
    this.isEditModalOpen = true;
  }

  closeEditModal(): void {
    this.isEditModalOpen = false;
    this.selectedItem = null;
    this.editErrors = {};
    this.pendingPhotoFile = null;
    this.pendingPhotoPreview = null;
  }

  createItem(): void {
    this.createErrors = {};
    if (!this.newItem.name?.trim()) {
      this.createErrors.name = 'Введите название блюда';
    } else if (this.newItem.name.length < 2) {
      this.createErrors.name = 'Название должно быть минимум 2 символа';
    }
    if (this.newItem.description && this.newItem.description.length > 500) {
      this.createErrors.description = 'Описание не должно превышать 500 символов';
    }
    if (!this.newItem.price || this.newItem.price <= 0) {
      this.createErrors.price = 'Цена должна быть больше 0';
    } else if (this.newItem.price > 100000) {
      this.createErrors.price = 'Цена не должна превышать 100000 рублей';
    }
    if (!this.newItem.subcategory?.trim()) this.createErrors.subcategory = 'Выберите подкатегорию';
    if (!this.newItem.volume?.trim()) this.createErrors.volume = 'Укажите объём или размер (например: 300 мл, S, L)';
    if (this.createErrors.name || this.createErrors.price || this.createErrors.subcategory || this.createErrors.description || this.createErrors.volume) return;

    const userId = this.authService.currentUser?.id ?? 1;
    const itemToCreate = { ...this.newItem };
    const photoFile = this.pendingPhotoFile;
    this.closeCreateModal();

    const doCreate = (fileId?: string) => {
      if (fileId) itemToCreate.photoFileId = fileId;
      this.menuService.createMenuItem(itemToCreate, userId).subscribe({
        next: (created) => {
          this.menuItems.push(created);
          this.filterItems();
          this.newItem = { name: '', description: '', price: 0, category: this.categories[0] || '', subcategory: '', photoFileId: '' };
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error creating menu item:', error);
          this.errorMessage = 'Не удалось создать блюдо. Попробуйте снова.';
          this.cdr.detectChanges();
        }
      });
    };

    if (photoFile) {
      this.isUploadingPhoto = true;
      this.menuService.uploadPhoto(photoFile).subscribe({
        next: (fileId) => { this.isUploadingPhoto = false; doCreate(fileId); },
        error: (err) => { this.isUploadingPhoto = false; console.error('Ошибка загрузки фото:', err); doCreate(); }
      });
    } else {
      doCreate();
    }
  }

  updateItem(): void {
    if (!this.selectedItem) return;
    this.editErrors = {};
    if (!this.selectedItem.name?.trim()) {
      this.editErrors.name = 'Введите название блюда';
    } else if (this.selectedItem.name.length < 2) {
      this.editErrors.name = 'Название должно быть минимум 2 символа';
    }
    if (this.selectedItem.description && this.selectedItem.description.length > 500) {
      this.editErrors.description = 'Описание не должно превышать 500 символов';
    }
    if (!this.selectedItem.price || this.selectedItem.price <= 0) {
      this.editErrors.price = 'Цена должна быть больше 0';
    } else if (this.selectedItem.price > 100000) {
      this.editErrors.price = 'Цена не должна превышать 100000 рублей';
    }
    if (!this.selectedItem.subcategory?.trim()) this.editErrors.subcategory = 'Выберите подкатегорию';
    if (!this.selectedItem.volume?.trim()) this.editErrors.volume = 'Укажите объём или размер (например: 300 мл, S, L)';
    if (this.editErrors.name || this.editErrors.price || this.editErrors.subcategory || this.editErrors.description || this.editErrors.volume) return;

    const item = { ...this.selectedItem };
    const oldItem = this.menuItems.find(i => i.id === item.id);
    const photoFile = this.pendingPhotoFile;
    this.closeEditModal();

    const doUpdate = (fileId?: string) => {
      if (fileId) item.photoFileId = fileId;
      const idx = this.menuItems.findIndex(i => i.id === item.id);
      if (idx >= 0) { this.menuItems[idx] = item; this.filterItems(); }
      this.menuService.updateMenuItem(item.id, item).subscribe({
        next: () => { this.cdr.detectChanges(); },
        error: (error) => {
          if (idx >= 0 && oldItem) { this.menuItems[idx] = oldItem; this.filterItems(); }
          console.error('Error updating menu item:', error);
          this.errorMessage = 'Не удалось обновить блюдо. Изменения отменены.';
          this.cdr.detectChanges();
        }
      });
    };

    if (photoFile) {
      this.isUploadingPhoto = true;
      this.menuService.uploadPhoto(photoFile).subscribe({
        next: (fileId) => { this.isUploadingPhoto = false; doUpdate(fileId); },
        error: (err) => { this.isUploadingPhoto = false; console.error('Ошибка загрузки фото:', err); doUpdate(); }
      });
    } else {
      doUpdate();
    }
  }

  deleteItem(item: MenuItem): void {
    if (confirm(`Удалить "${item.name}"?`)) {
      // Сразу удаляем из списка UI
      const oldItems = this.menuItems;
      this.menuItems = this.menuItems.filter(i => i.id !== item.id);
      this.filterItems();
      // Отправляем запрос
      this.menuService.deleteMenuItem(item.id).subscribe({
        next: () => { /* уже удалили из UI выше */ },
        error: (error) => {
          this.menuItems = oldItems;
          this.filterItems();
          console.error('Error deleting menu item:', error);
          this.errorMessage = 'Не удалось удалить блюдо. Попробуйте снова.';
        }
      });
    }
  }

  toggleAvailability(item: MenuItem): void {
    // Сразу меняем UI (оптимистичное обновление)
    const oldValue = item.isAvailable;
    item.isAvailable = !item.isAvailable;
    // Отправляем запрос на сервер
    this.menuService.toggleAvailability(item.id, item.isAvailable).subscribe({
      next: () => { /* уже обновили UI выше */ },
      error: (error) => {
        // Если ошибка — откатываем
        item.isAvailable = oldValue;
        console.error('Error toggling availability:', error);
      }
    });
  }

  toggleVisibility(item: MenuItem): void {
    // Сразу меняем UI (оптимистичное обновление)
    const oldValue = item.isHidden;
    item.isHidden = !item.isHidden;
    // Отправляем запрос на сервер
    this.menuService.toggleVisibility(item.id, item.isHidden).subscribe({
      next: () => { /* уже обновили UI выше */ },
      error: (error) => {
        // Если ошибка — откатываем
        item.isHidden = oldValue;
        console.error('Error toggling visibility:', error);
      }
    });
  }

  goBack(): void { this.router.navigate(['/dashboard']); }

  onPhotoSelected(event: Event, isEdit: boolean = false): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    this.pendingPhotoFile = file;
    if (this.pendingPhotoPreview) URL.revokeObjectURL(this.pendingPhotoPreview);
    this.pendingPhotoPreview = URL.createObjectURL(file);
  }
}
