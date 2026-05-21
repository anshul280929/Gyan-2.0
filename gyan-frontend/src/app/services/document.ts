import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {

  private baseUrl = 'http://localhost:8080/documents';

  constructor(private http: HttpClient) {}

  getDocuments() {
    return this.http.get(this.baseUrl);
  }

  uploadDocument(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post('http://localhost:8080/documents/upload', formData);
  }
}