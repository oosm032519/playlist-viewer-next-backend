# Playlist Viewer Next.js バックエンド

## 概要

このプロジェクトは、Spotify のプレイリストを閲覧、分析、操作するための Spring Boot バックエンドアプリケーションです。Next.js
フロントエンドアプリケーションと連携して動作し、Spotify API との通信、データ処理、認証、セッション管理などを担当します。

**[デモサイト](https://playlist-viewer-next-frontend.vercel.app)**

## 主な機能

* **プレイリスト操作**:
    * プレイリストの検索、閲覧
    * プレイリストの詳細情報の取得
    * プレイリストへのトラックの追加・削除
    * 新しいプレイリストの作成
    * お気に入りのプレイリストの管理
* **プレイリスト分析**:
    * ジャンル分布の表示
    * Audio Features の分析 (danceability, energy, valence など)
    * 推奨トラックの生成
* **ユーザー認証**:
    * Spotify OAuth2 を使用した安全な認証
* **セッション管理**:
    * Redis を使用したセッション管理
* **データベース**:
    * お気に入りのプレイリスト情報を MySQL に保存

## 技術スタック

* **フレームワーク**: Spring Boot
* **認証**: Spring Security, Spotify OAuth2, JWT
* **データベース**: MySQL, Spring Data JPA
* **キャッシュ**: Redis, Spring Data Redis
* **Spotify API**: Spotify Web API Java
* **暗号化**: Tink
* **その他**: Lombok, Swagger

## 開発環境のセットアップ

1. **前提条件**:
    * Java Development Kit (JDK) 17 以上
    * Maven
    * MySQL
    * Redis
2. **リポジトリのクローン**:
    ```bash
    git clone https://github.com/oosm032519/playlist-viewer-next-backend.git
    ```
3. **環境変数の設定**:
    * `application.properties` ファイルをコピーして `application-development.properties` を作成します。
    * `application-development.properties` に以下の環境変数を設定します。
        * `SPOTIFY_CLIENT_ID`: Spotify API のクライアント ID
        * `SPOTIFY_CLIENT_SECRET`: Spotify API のクライアントシークレット
        * `FRONTEND_URL`: フロントエンドアプリケーションの URL
        * `BACKEND_URL`: バックエンドアプリケーションの URL
        * `JAWSDB_URL`: MySQL データベースの URL
        * `SPRING_DATASOURCE_USERNAME`: MySQL データベースのユーザー名
        * `SPRING_DATASOURCE_PASSWORD`: MySQL データベースのパスワード
        * `PORT`: アプリケーションのポート番号
        * `JWT_SECRET`: JWT のシークレットキー
        * `REDIS_TLS_ENABLED`: Redis の TLS 接続を有効にするかどうか
        * `REDIS_TLS_URL`: Redis の TLS 接続 URL
4. **依存関係のインストール**:
    ```bash
    mvn clean install
    ```
5. **アプリケーションの起動**:
    ```bash
    mvn spring-boot:run
    ```

## API ドキュメント

Swagger UI を使用して API ドキュメントを参照できます。アプリケーションを起動した後、ブラウザで以下の URL にアクセスしてください。

http://localhost:8080/swagger-ui/index.html

## ライセンス

MIT License

## 免責事項

このプロジェクトは、Spotify とは一切関係ありません。
