<h1 align="center">🎁 Pockon - 내 손안의 기프티콘 매니저</h1>

---

## 🚀 정식 출시 · 바로 사용하기
**Pockon - 내 손안의 기프티콘 매니저**  
구글 플레이 스토어에 정식 출시된 앱입니다.  
지금 바로 다운로드하여 직접 사용해보세요!

👉 [구글 플레이 스토어에서 다운로드](https://play.google.com/store/apps/details?id=com.sumi.pockon)

---

## 📱 프로젝트 설명 (App Overview)
**Pockon**은 기프티콘을 쉽고 간편하게 저장하고 관리할 수 있는 안드로이드 앱입니다. 기프티콘을 갤러리에만 저장해두면 쉽게 흩어져 찾기 힘들고, 만료일을 지나쳐 버릴 위험이 있습니다. Pockon은 이런 문제를 해결하고, 사용자가 기프티콘을 **효율적으로 관리하고 만료일을 놓치지 않도록** 돕습니다.

뿐만 아니라, **현재 위치를 기반으로 근처에서 사용할 수 있는 기프티콘**을 바로 확인할 수 있어, 실시간으로 유용한 기프티콘을 쉽게 찾을 수 있습니다. Pockon은 직관적인 UI와 알림 기능을 통해 사용자에게 **편리하고 유용한 기프티콘 관리 경험**을 제공합니다.


---

## 🚀 주요 기능 (Features)
- 🎫 **기프티콘 등록 및 관리**: 기프티콘을 쉽게 등록하고 카테고리별로 정리할 수 있습니다.
- ⏳ **만료 임박 알림**: 기프티콘의 만료일이 가까워지면 푸시 알림으로 알려줍니다.
- 📍 **위치 기반 사용처 안내**: 현재 위치를 기반으로 기프티콘 사용 가능한 매장을 지도에서 확인할 수 있습니다.
- 🔐 **구글 로그인 및 게스트 모드**: 구글 계정으로 로그인하거나 로그인 없이도 앱을 사용할 수 있습니다.
- 🌙 **다크모드 지원**: 눈의 피로를 덜어주는 다크모드를 제공합니다.
- 🖌️ **직관적이고 간편한 UI/UX**: 누구나 쉽게 사용할 수 있도록 설계된 심플하고 직관적인 사용자 환경을 제공합니다.

---

## 🛠 사용 기술 (Tech Stack)

| 구분 | 사용 기술 |
| --- | --- |
| 언어·플랫폼 | Kotlin, Android SDK |
| 아키텍처·상태 관리 | Clean Architecture, MVVM, ViewModel, StateFlow, SharedFlow, UiState |
| UI | Jetpack Compose, Material 3, ViewBinding, Navigation Component / Navigation Compose |
| 의존성 주입 | Hilt (Dagger) |
| 데이터 | Room, Firebase Authentication, Cloud Firestore, Firebase Storage |
| 인증 | Credential Manager, Google Identity Services, Google Sign-In |
| 네트워크·직렬화 | Retrofit, Gson, Kakao Local API |
| 지도·위치 | Naver Map SDK, Google Play Services Location |
| 이미지 | Coil, ExifInterface |
| 알림 | AlarmManager |
| 기타 UI | ViewPager2, SwipeRefresh |

---

## 🏗️ 아키텍처 및 리팩터링

```text
UI (Compose / Fragment)
        ↓
ViewModel (UiState + Event)
        ↓
UseCase → Repository
        ↓
Local (Room) / Remote (Firebase · Kakao API)
```

- 화면 상태를 `UiState`로 통합하고, Snackbar·화면 전환처럼 한 번만 처리할 동작은 이벤트로 분리했습니다.
- `Gift`, `BrandLocation`을 domain 모델로 두고 Firebase/Kakao DTO 및 Room Entity·local 모델과 분리했습니다.
- Repository mapper가 remote/local 모델과 domain 모델 사이의 변환을 담당합니다.

```text
domain: Gift, BrandLocation
remote: GiftDto, BrandSearchDto, PlaceDocumentDto
local: GiftEntity, BrandEntity, BrandLocationLocal
```

---

## 📸 화면 및 기능 소개
### 1. **로그인 화면**  
구글 로그인과 게스트 모드를 지원하며, 로그인 후 PIN 설정을 통해 보안을 강화할 수 있습니다.
#### 1) 로그인 선택 화면
- 구글 계정 로그인 또는 게스트 모드 접속을 지원합니다.
- 구글 계정 로그인 시 개인정보 수집 및 이용 동의서에 동의해야 서비스를 이용할 수 있습니다.
- 게스트 모드 접속 시 동의서 동의 없이 즉시 이용할 수 있습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/029750c3-dd09-40b9-ab43-1f7c6f19aa68" width="250" />
  <img src="https://github.com/user-attachments/assets/a3789fe9-deb8-4533-bf31-8abe89400af8" width="250" />
  <img src="https://github.com/user-attachments/assets/971865f0-5b85-4e71-a5fc-7d2a0b158465" width="250" />
</p>

#### 2) PIN 로그인
- PIN 설정 후, 앱 실행 시 PIN 입력을 통해 잠금 해제가 필요합니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/d92e4027-82f4-45ce-958e-017cfeb77529" width="250" />
  <img src="https://github.com/user-attachments/assets/ab470c2b-26a5-4a94-ae13-acc769b772ea" width="250" />
</p>

### 2. **홈 화면**  
앱의 메인 화면으로, 근처에서 사용 가능한 기프티콘과 찜한 기프티콘을 구분하여 보여줍니다.<br>
<p float="left">
  <img src="https://github.com/user-attachments/assets/a5f4a567-9161-4842-97cd-84d470dd36cf" width="250" />
</p>

#### 1) 지도 보기 및 사용처 확인
- ‘지도 보기’ 버튼 클릭 시, 현재 위치를 기준으로 근처 사용 가능한 매장이 지도에 마커로 표시됩니다.
- 마커 클릭 시, 해당 매장에서 사용할 수 있는 기프티콘 목록을 뷰페이저 형식으로 스와이프하며 확인할 수 있습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/14f011a5-5db3-4f96-9652-d5c968719ea9" width="250" />
</p>

#### 2) 찜한 기프티콘 관리
- 기프티콘의 사용 가능 여부 및 만료 여부와 관계없이 자유롭게 찜 등록이 가능합니다.
- 찜한 기프티콘은 홈 화면의 별도 섹션에 구분되어 손쉽게 모아볼 수 있습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/3d22ee94-ff7d-424c-a41a-be3ae8237f80" width="250" />
</p>


### 3. **기프티콘 목록 화면**  
등록된 기프티콘을 편리하게 관리할 수 있는 목록 화면입니다.
#### 1) 정렬 · 삭제 · 브랜드 필터링
- 최신순, 만료일순, 가나다순으로 목록 정렬이 가능합니다.
- 체크 박스를 통해 여러 기프티콘을 한 번에 선택하여 전체 삭제 또는 선택 삭제를 할 수 있습니다.
- 상단 브랜드 카테고리 탭을 통해 원하는 브랜드의 기프티콘만 쉽게 필터링할 수 있습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/5ec7d5aa-e41d-4f48-89c1-9e094cb3a10c" width="250" />
  <img src="https://github.com/user-attachments/assets/b72c0e0f-36fc-42a8-b6a3-8f842f134cc0" width="250" />
  <img src="https://github.com/user-attachments/assets/e0d212fa-32b4-4e45-9bb9-96b23a968d54" width="250" />
</p>

#### 2) 제스처 및 새로고침 기능
- 스와이프 제스처로 빠르게 조작할 수 있습니다.<br>
   오른쪽에서 왼쪽으로 스와이프 시, 해당 기프티콘이 사용 완료 처리됩니다.<br>
   왼쪽에서 오른쪽으로 스와이프 시, 해당 기프티콘이 삭제됩니다.
- 화면을 아래로 당기면 새로고침 기능이 작동하여 최신 데이터로 목록이 갱신됩니다.
 <p float="left">
  <img src="https://github.com/user-attachments/assets/800d84e1-3598-40a1-a869-8aa9ae3cd470" width="250" />
  <img src="https://github.com/user-attachments/assets/18ccf2d0-f12c-4342-9638-dcca2a04439c" width="250" />
</p>

### 4. **기프티콘 추가 및 관리 화면**  
기프티콘을 손쉽게 등록하고 상세하게 관리할 수 있는 화면입니다.

#### 1) 상세 정보 등록 및 편집
- 사진, 이름, 브랜드, 만료일, 메모, 금액(금액권) 등 기프티콘의 상세 정보를 등록·편집할 수 있습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/99a6422f-2d55-4587-a336-3ae0544e1780" width="250" />
  <img src="https://github.com/user-attachments/assets/d0f8f009-18b4-4bf6-bfed-2e907afd44e2" width="250" />
  <img src="https://github.com/user-attachments/assets/0fe93570-e974-445d-b871-7b75faad4512" width="250" />
</p>

#### 2) 사용 완료 및 바코드 확인
- 사용 완료 버튼 클릭 시, 기프티콘 전체 이미지가 바텀 다이얼로그로 확대 표시됩니다.
- 바텀 다이얼로그 내 이미지를 클릭하면 자유롭게 확대·축소할 수 있습니다.
- 사용 완료 처리 시, 사용 일시가 이미지 상단에 오버레이 라벨로 표시되어 사용 상태와 사용 날짜를 한눈에 확인할 수 있습니다.
<p align="left">
  <img src="https://github.com/user-attachments/assets/8686fec8-6c48-469b-a818-d3075007045f" width="250"/>
  <img src="https://github.com/user-attachments/assets/6107eeae-5fcc-46e1-a86e-fd0b6706fec5" width="250"/>
</p>

#### 3) 금액권 잔액 자동 계산
- 금액권은 사용 금액 입력 시 잔액이 즉시 계산되며, 잔액이 0원이 되면 사용 완료 처리됩니다.
<p align="left">
  <img src="https://github.com/user-attachments/assets/73739fc2-ec8c-402a-906d-806b608f57cd" width="250"/>
</p>


### 5. **설정 화면**  
사용자 계정 정보 및 앱 환경을 관리할 수 있는 화면입니다.
#### 1) 사용자 계정 정보 표시
- 로그인한 사용자 이름과 이메일을 확인할 수 있습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/08ac182d-7947-4772-94d3-1a81fb8d58d7" width="250" />
</p>

#### 2) 사용 내역 조회
- 사용 완료된 기프티콘 내역을 조회할 수 있으며, 전체 삭제 및 선택 삭제가 가능합니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/75030c0a-dbeb-41cf-b112-7df8a7441330" width="250" />
  <img src="https://github.com/user-attachments/assets/f1a66dbb-f3ba-4c14-a4f6-d22ba4f47ded" width="250" />
</p>

#### 3) 만료 임박 알림 설정
- 기프티콘 만료 알림을 ON/OFF로 설정할 수 있습니다.
- 사용자가 원하는 시간대와 알림 기준일을 자유롭게 설정할 수 있습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/01dd692b-ee9a-469f-9f80-70ccaa54af9a" width="250" />
  <img src="https://github.com/user-attachments/assets/40d5ff63-ea1b-4b43-8128-09ac8cabc77d" width="250" />
  <img src="https://github.com/user-attachments/assets/2848f9f4-a552-4a28-8f85-850040f36e30" width="250" />
</p>

#### 4) PIN(비밀번호) 사용 설정
- 앱 실행 시 PIN 입력 요구를 ON/OFF로 설정할 수 있습니다.
- ON 설정 시 즉시 비밀번호 생성 화면으로 이동하여 PIN을 생성할 수 있습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/5f16c45c-a0ab-4330-9c0c-cd0cb808284f" width="250" />
  <img src="https://github.com/user-attachments/assets/d894ddee-6632-428d-b8a4-188ae449a3a4" width="250" />
</p>

#### 5) 로그아웃 및 회원 탈퇴
- 현재 계정에서 로그아웃하거나, 회원 탈퇴 시 계정 및 데이터가 삭제됩니다.
- 게스트 모드에서는 ‘로그아웃’ 및 ‘회원 탈퇴’ 메뉴가 표시되지 않으며, ‘게스트 모드 종료’ 메뉴만 제공됩니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/9833236a-f1ed-41ac-ac24-a1361a1ce86f" width="250" />
  <img src="https://github.com/user-attachments/assets/c35041fc-e8ef-4ee3-822d-1d1d2e2fd92e" width="250" /> 
  <img src="https://github.com/user-attachments/assets/faee1d3e-fb84-4028-a404-85bb55bf3fae" width="250" />
</p>

### 6. **다크모드 및 화면 회전 지원**  
#### 1) 다크모드 지원
- 시스템 설정에 따라 앱 테마가 라이트/다크 모드로 자동 전환됩니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/9cdb1740-42b8-43a6-a23c-0b74da0c6e64" width="250" />
  <img src="https://github.com/user-attachments/assets/9b5ee5d0-ecd3-486f-a724-57009ec8a31c" width="250" />
  <img src="https://github.com/user-attachments/assets/41a4a00d-75ab-4b82-a9d5-0d152a254b57" width="250" />
</p>

#### 2) 화면 회전 지원
- ViewModel을 통해 상태를 관리하여 화면 회전 시에도 데이터가 유지됩니다.
- 화면 회전 시 입력 데이터 및 화면 구성이 초기화되지 않도록 안정성에 신경 썼습니다.
<p float="left">
  <img src="https://github.com/user-attachments/assets/7c4f5213-a448-4403-b532-196c4c4078ce" width="250" />
  <img src="https://github.com/user-attachments/assets/252bbf72-12be-4060-b306-ecb07c0cefb6" width="250" />
</p>

---

## ⚙️ 설치 방법 (Installation)
1. 이 프로젝트를 클론합니다:<br>
   `git clone https://github.com/sumi-yoo/Pockon.git`
2. Android Studio에서 프로젝트를 엽니다.
3. 필요한 의존성 파일을 설치합니다:<br>
   `./gradlew build`
4. 에뮬레이터나 실제 디바이스에서 앱을 실행합니다.

---

## ✉️ 연락처 (Contact)
- 이메일: sumi.yoo.dev@gmail.com
