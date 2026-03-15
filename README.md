# ⚙️ Testmate
**Automated Exam Management & Real-time Scoring Platform**

<p align="left">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
  <img src="https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black">
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white">
</p>

---

## ✨ Project Preview
**Testmate**는 교육 콘텐츠 관리의 비효율성을 해결하기 위한 통합 솔루션임
단순 문제 풀이를 넘어 **PDF 자동 파싱 기술**과 **데이터 무결성 검증 대시보드**를 통해 교육 데이터의 디지털화 및 정합성 관리 자동화를 구현함

### 1. Smart Content Authoring (Admin) - PDF Parser
PDF 파싱 기술을 이용한 시험 콘텐츠의 신속한 디지털화 지원

<p align="center">
  <img src="https://github.com/user-attachments/assets/c9e1ab60-b0e7-4c35-85fc-0e9ee7840682" width="70%" />
  <br>
  <em>▲ PDF 자동 파싱 등록 프로세스</em>
</p>

---

### 2. Smart Content Authoring (Admin) - Intuitive Editor
직관적인 에디터를 활용한 문항 직접 작성 및 세밀한 구성 기능 제공

<p align="center">
  <img src="https://github.com/user-attachments/assets/9276b421-ab80-47ed-a063-2056c1caa0a4" width="70%" />
  <br>
  <em>▲ 에디터 직접 작성 등록 프로세스</em>
</p>

---

### 3. Data Integrity Dashboard (Admin)
정답지 누락 등 불완전 시험 데이터 실시간 탐지 및 대시보드 내 즉각적인 정합성 복구 지원

<p align="center">
  <img src="https://github.com/user-attachments/assets/f3f732c3-cef3-4b17-907e-15b77a1fdade" width="70%" />
  <br>
  <em>▲ Chart.js 기반 등록 현황 모니터링 및 누락 정답지 즉시 매핑</em>
</p>

---

### 4. Real-time Assessment & Feedback (User)
실제 시험 환경과 유사한 UX 환경 구축 및 제출 즉시 서버 사이드 채점 결과와 동적 시각적 피드백 제공

<p align="center">
  <img src="https://github.com/user-attachments/assets/48e3124d-5799-4c9f-be8e-0ff1e34b5a6a" width="70%" />
  <br>
  <em>▲ 사용자 응시 프로세스 및 Canvas API 기반 동적 피드백 연출</em>
</p>

---

## 🛠 Engineering Focus
- **Robust Data Integrity**: `Self-Join` 기반 무결성 검증 아키텍처를 채택하여 정답지가 확보된 유효 시험 데이터만 서비스 레이어에 노출되도록 설계함
- **Visual Data Analytics**: `Chart.js`를 통해 시험지 등록 및 정답지 누락 현황을 시각화함. 관리자가 데이터 불완전성을 즉각 인지하고 즉시 보완 가능한 운영 환경을 구축함
- **Storage Optimization**: 방대한 문항 및 텍스트 데이터를 안정적으로 제어하기 위해 `CLOB` 데이터 타입을 활용하여 DB 스토리지 효율을 최적화함

---

## 📅 Roadmap
- [ ] **Granular Learning**: 단일 문항 집중 학습 및 리뷰 모드 고도화
- [ ] **Randomized Mock Exam**: 전 회차 데이터 기반 무작위 문항 추출 알고리즘 적용
- [ ] **Smart Note**: 개인별 오답 자동 저장 및 맞춤형 메모 시스템 구현
- [ ] **Performance Analytics**: 응시 누적 데이터를 활용한 성적 변화 추이 시각화 리포트 제공

---

## ⚖️ License
Copyright (c) 2026 (Your Name). This project is licensed under the [MIT License](./LICENSE).
