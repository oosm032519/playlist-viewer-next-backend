**Playlist Viewer - バックエンド プロジェクト概要**

Playlist Viewerは、Spotifyのプレイリストをより詳細に閲覧・分析するためのWebアプリケーションです。このバックエンドは、Spring
Bootフレームワークを使用して開発されており、フロントエンドからのリクエストを処理し、Spotify
APIと連携して必要なデータを提供します。主な機能として、ユーザー認証、プレイリスト情報の取得、楽曲分析、お気に入り登録、プレイリスト作成などが含まれます。

**使用技術**

- **Java**
- **Spring Boot**
- **Spring Security:** 認証および認可
- **Spring Data JPA:** データベースアクセス
- **Spring Data Redis:** Redisデータアクセス
- **Spotify Web API Java:** Spotify APIクライアントライブラリ
- **MySQL**
- **Redis:** セッション管理、キャッシュ
- **flyway:** データベースマイグレーションツール
- **Lombok:** ボイラープレートコード削減
- **OpenAPI (SpringDoc):** APIドキュメンテーション生成
- **AspectJ**
- **java-faker:** モックデータ生成

**セットアップ方法**

1. **リポジトリのクローン:**
   ```bash
   git clone https://github.com/oosm032519/playlist-viewer-next.git
   cd playlist-viewer-next/playlist-viewer-next-backend
   ```

2. **環境変数の設定:**
   `src/main/resources/application.properties` ファイルを編集し、以下の環境変数を設定してください。

   ```
   spotify.client.id=YOUR_SPOTIFY_CLIENT_ID
   spotify.client.secret=YOUR_SPOTIFY_CLIENT_SECRET
   frontend.url=http://localhost:3000
   backend.url=http://localhost:8080
   spring.datasource.url=YOUR_DATABASE_URL
   spring.datasource.username=YOUR_DATABASE_USERNAME
   spring.datasource.password=YOUR_DATABASE_PASSWORD
   spring.data.redis.url=redis://localhost:6379
   spotify.mock.enabled=true
   ```

    - `spotify.client.id`: SpotifyアプリケーションのクライアントID
    - `spotify.client.secret`: Spotifyアプリケーションのクライアントシークレット
    - `frontend.url`: フロントエンドアプリケーションのURL
    - `backend.url`: バックエンドアプリケーションのURL
    - `spring.datasource.url`: データベースのURL
    - `spring.datasource.username`: データベースのユーザー名
    - `spring.datasource.password`: データベースのパスワード
    - `spring.data.redis.url`: RedisサーバーのURL
    - `spotify.mock.enabled`: モックモードを使用するかどうか (true/false)

3. **依存関係のインストールとビルド:**
   ```bash
   ./mvnw clean install
   ```

4. **アプリケーションの実行:**
   ```bash
   ./mvnw spring-boot:run
   ```

   アプリケーションはデフォルトでポート8080で起動します。

**主な機能**

- **ユーザー認証:**
    - Spotify OAuth 2.0認証をサポートしています。
    - モックモードでは、モックログインが可能です。
    - 認証成功後、セッションIDを生成し、Redisに保存します。
    - セッションIDはCookieに保存され、クライアントとの認証に使用されます。
- **プレイリスト検索:**
    - Spotify APIを使用してプレイリストを検索します。
    - 検索結果はページネーションで取得できます。
    - 検索結果はキャッシュされます。
- **プレイリスト詳細取得:**
    - プレイリストの基本情報（名前、作成者、楽曲数など）を取得します。
    - プレイリストに含まれる楽曲のリストを取得します。
    - 各楽曲のオーディオ特徴量（テンポ、エネルギー、ダンス性など）を取得します。
    - プレイリストのジャンルを集計し、出現頻度の高いジャンルを特定します。
    - プレイリストのオーディオ特徴量の平均値、最大値、最小値を計算します。
- **おすすめ楽曲取得:**
    - ユーザーが選択したアーティストと、プレイリストのオーディオ特徴量に基づいて、おすすめ楽曲を取得します。
- **お気に入り登録:**
    - ユーザーはプレイリストをお気に入り登録できます。
    - お気に入り登録されたプレイリストはデータベースに保存されます。
    - ユーザーは自分のお気に入りプレイリスト一覧を取得できます。
    - プレイリストがお気に入り登録されているかどうかを確認できます。
- **プレイリスト作成:**
    - ユーザーは新しいプレイリストを作成できます。
    - プレイリストには、指定された楽曲を追加できます。
- **プレイリストへのトラック追加・削除:**
    - ユーザーは既存のプレイリストにトラックを追加できます。
    - ユーザーは既存のプレイリストからトラックを削除できます。
- **セッション管理:**
    - セッションIDを使用してユーザーを認証します。
    - セッション情報はRedisに保存されます。
    - セッションの有効期限は1時間です。
- **エラーハンドリング:**
    - 発生したエラーは適切に処理され、エラーメッセージとステータスコードを含むエラーレスポンスが返されます。
- **APIドキュメント:**
    - OpenAPI (Swagger) を使用してAPIドキュメントを生成しています。

**APIエンドポイント**

- **認証:**
    - `POST /api/session/mock-login`: モックログイン
    - `GET /api/session/check`: セッションチェック
    - `POST /api/session/logout`: ログアウト
    - `POST /api/session/sessionId`: セッションID取得
- **プレイリスト検索:**
    - `GET /api/playlists/search`: プレイリスト検索
- **プレイリスト詳細:**
    - `GET /api/playlists/{id}/details`: プレイリスト詳細取得
- **おすすめ楽曲:**
    - `POST /api/playlists/recommendations`: おすすめ楽曲取得
- **お気に入り:**
    - `POST /api/playlists/favorite`: お気に入り登録
    - `DELETE /api/playlists/favorite`: お気に入り解除
    - `GET /api/playlists/favorites`: お気に入り一覧取得
    - `GET /api/playlists/favoriteCheck`: お気に入り確認
- **プレイリスト作成:**
    - `POST /api/playlists/create`: プレイリスト作成
- **トラック追加・削除:**
    - `POST /api/playlist/add-track`: トラック追加
    - `POST /api/playlist/remove-track`: トラック削除
- **フォロー中プレイリスト:**
    - `GET /api/playlists/followed`: フォロー中プレイリスト取得

**今後の展望**

- ユーザー認証の強化
- プレイリスト編集機能の拡充
- より高度な分析機能の追加
- パフォーマンスの最適化

**連絡先**

ご質問やご意見がございましたら、oosm032519@gmail.com までお気軽にご連絡ください。
