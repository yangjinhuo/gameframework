-- Default accounts are initialized by the Spring Boot application on startup
-- (see com.scriptplatform.config.DataInitializer). You may also insert them manually.
--
-- Default credentials:
--   admin  / Admin@123     (super_admin)
--   devops / Devops@123    (admin)
--   viewer / Viewer@123    (readonly)

USE `script_platform`;

-- Sample script (optional)
INSERT INTO `script_info` (
    `script_name`, `script_path`, `module`, `description`, `owner`,
    `alert_dingtalk`, `alert_phone`, `schedule_cron`, `schedule_desc`,
    `script_content`, `version`, `status`
) VALUES (
    'daily_report.py',
    '/opt/scripts/daily_report.py',
    '报表生成',
    '每日业务数据汇总报表脚本',
    'devops',
    'https://oapi.dingtalk.com/robot/send?access_token=xxx',
    '13800000000',
    '0 0 8 * * ?',
    '每天 08:00 执行',
    '#!/usr/bin/env python3\n# -*- coding: utf-8 -*-\n"""Daily report script."""\n\nimport datetime\n\n\ndef main():\n    today = datetime.date.today()\n    print(f"[{today}] generating daily report ...")\n\n\nif __name__ == "__main__":\n    main()\n',
    1,
    1
);
