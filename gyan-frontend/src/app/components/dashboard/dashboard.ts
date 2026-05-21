import { Component, OnInit } from '@angular/core';
import { DocumentService } from '../../services/document';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { AIService } from '../../services/ai';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, FormsModule],
  templateUrl: './dashboard.html',
})
export class DashboardComponent implements OnInit {

  documents: any[] = [];
  selectedFile: File | null = null;
  uploading = false;
  question: string = '';
  answer: string = '';
  loadingAnswer = false;

  constructor(
    private documentService: DocumentService,
    private aiService: AIService
  ) {}

  ngOnInit() {
    this.loadDocuments();
  }

  loadDocuments() {
    this.documentService.getDocuments().subscribe({
      next: (res: any) => {
        this.documents = res.content || res;
      },
      error: (err) => console.error(err)
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  upload() {
    if (!this.selectedFile) {
      alert("Select a file first");
      return;
    }

    this.uploading = true;

    this.documentService.uploadDocument(this.selectedFile).subscribe({
      next: () => {
        this.uploading = false;
        this.selectedFile = null;
        this.loadDocuments();
      },
      error: (err) => {
        this.uploading = false;
        console.error(err);
      }
    });
  }


  askAI() {
    if (!this.question) {
      alert("Enter a question");
      return;
    }

    this.loadingAnswer = true;

    this.aiService.askQuestion(this.question).subscribe({
      next: (res: any) => {
        this.answer = res.answer;
        this.loadingAnswer = false;
      },
      error: (err) => {
        console.error(err);
        this.loadingAnswer = false;
      }
    });
  }

  logout() {
    localStorage.removeItem('token');
    location.href = '/';
  }
}