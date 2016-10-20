# BehaviorSubjectSample

MainActivity
- TabLayout + ViewPager を持つ
- ViewPager の中身は MainFragment
- 各タブで共通のデータ（CommonData）をサーバーから取得する
- PullToRefresh で共通データを取り直し、各タブの MainFragment にもデータを取り直させる

MainFragment
- タブ特有のデータ（SpecificData）をサーバーから取得する
- 共通データ（CommonData）を MainActivity から取得する
- 共通データとタブ特有のデータを取得し終わるまでプログレスを表示する

