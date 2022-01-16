# 서비스명

**글 한 조각 ( A Piece Of Writing )**

    좋아하는 문장을 모으고 공유하는 서비스
    
```
책이나 시나 어디에서 읽은 글이든 마음에 가장 와닿는 문장, 구절이 있기 마련이고, 그걸 아카이빙 해놓으면 어떨까 했습니다.

사람마다 힘들 때, 위로가 필요할 때 도움이 되었던 구절이나 나의 상황과 너무 비슷해서 뇌리에 강하게 남은 구절은 있기 마련입니다. 단순히 SNS에서 잠깐 스쳐보고 지나간 문구여도 강렬했다면 기억에 남겠지요. 하지만 사람은 망각의 동물입니다. 단순 기억은 20분이면 사라지고 이틀 뒤면 30%만이 기억에 남습니다. 내가 좋아하는 문장과 구절을 아무리 외운다고 해도 대부분은 잊어버리게 됩니다.

내가 지금 힘들고 지쳤을 때, 누군가가 건네는 위로의 한 마디가 절실한 상황에 어떤 구절을 읽고 위로받은 경험이 다들 있을겁니다. 그 문장들을 모아놓으면, 단순히 단어만 나열해놓은 게 아니고 그 감정과 기억을 모두 모아놓는 게 됩니다.

언제든 꺼내볼 수 있는 일기장과 같은 것이지요.
```


# 📋 컨벤션
## 브랜치 관리 전략
⚙️ github-flow

![](https://lh3.googleusercontent.com/h5H7FB2-aBPVThE4ZlZt919Fl9CstlD17NlJoODMKOlMEHmEV0encsCR2KmJ4yc6JwMsqoyv7u3jWVtW17Q3EqcHzPxUya85fRwRjgDlL2BapLtarQiu-SnjpUjyC2weng-PAXwx)



| 브랜치 종류  | 설명                                                         |
| ------------ | ------------------------------------------------------------ |
| Master(main) | 테스트 서버에서 테스트가 끝나고 운영서버로 배포 할 수 있는 브랜치 |
| feature      | 하나의 기능을 개발하기 위한 브랜치                           |
| release | 이번 출시 버전을 준비하는 브랜치             |

### 참고 자료
1. [Git 브랜칭 전략 : Git-flow와 Github-flow](https://hellowoori.tistory.com/56)
2. [GitHub flow](https://docs.github.com/en/get-started/quickstart/github-flow)

## 브랜치 네이밍
⚙️ 네이밍 패턴

브랜치 종류/이슈번호-간단한 설명	

Ex) 이슈번호가 67인 '로그인 기능' 이슈를 구현하는 브랜치를 생성하는 경우, 브랜치 이름을

*feature/67-login* 로 작성한다.

-> 논의 후 수정 예정

## 커밋 메시지


**⚙️ Type**

| 타입 종류 | 설명                                 |
| --------- | ------------------------------------ |
| feat      | 새로운 기능에 대한 커밋              |
| fix       | 수정에 대한 커밋                     |
| bug       | 버그에 대한 커밋                     |
| build     | 빌드 관련 파일 수정에 대한 커밋      |
| ci/cd     | 배포 커밋                            |
| docs      | 문서 수정에 대한 커밋                |
| style     | 코드 스타일 혹은 포맷 등에 관한 커밋 |
| refactor  | 코드 리팩토링에 대한 커밋            |
| test      | 테스트 코드 수정에 대한 커밋         |


⚙️ 구조

[Type] 제목 #이슈번호

본문

Ex) 이슈번호가 67인 이슈의 기능을 구현한 뒤 커밋을 하는 상황이라면 커밋 메시지의 제목을

*[feat] A기능 구현 #67* 으로 작성한다.

-> 논의 후 수정 예정

## 🗺️아키텍처

### Frontend

![image](https://user-images.githubusercontent.com/52997401/149660841-e8865390-c461-41fa-a049-76e0752c7e33.png)

### Backend

![image](https://user-images.githubusercontent.com/52997401/149660822-34ee591c-c687-4722-9517-a529fc1ac37f.png)

## ⚒️ 기술 스택

### Backend
-   Java, SpringBoot, IntelliJ
-   Naver Auth, Google Auth, Spring Security
-   Git Travis
-   AWS EC2, S3, CodeDeploy
-   Junit, JPA
-   Nginx

## 👥 파트 및 개발 계획
### [ 팀원 & 파트 ]
#### 🖥️ 프론트엔드

#### 🗄️ 백엔드
- 신동민 [Github](https://github.com/carnival77)

#### 🎨 디자인


[ 개발 기간 ] 2022/01 ~ 2022/02

📑Notion: https://friendly-chips-ca7.notion.site/10-c7cc990a5a6c4564966e2e72c2a7fe78
