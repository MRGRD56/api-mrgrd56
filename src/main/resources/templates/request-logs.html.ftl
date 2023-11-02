<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>HTTP Requests Logs</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:ital@0;1&family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap"
              rel="stylesheet">

        <style>
            html {
                background-color: #1e1f22;
            }

            .http-log {
                margin: 20px 20px 30px 20px;
                color: #bcbec4;
                font-family: 'JetBrains Mono', monospace;
                font-size: 0.75rem;
                line-height: 1.175rem;
            }

            .time {
                color: #5f826b;
                font-size: 0.6rem;
            }

            .method {
                color: #cf8e6d;
            }

            .protocol {
                /* color: #cf8e6d; */
            }

            .uri {
                color: #a9b7c6;
            }

            .header-name {
                color: #c77dbb;
                font-style: italic;
            }

            .header-value {
                color: #bcbec4;
            }

            .body {
                color: #6aab73;
            }
        </style>
    </head>

    <script src="/static/node_modules/moment/min/moment.min.js"></script>

    <body>
        <div class="http-logs">
            <#list requests as request>
                <div class="http-log" id="http-log-${request.id}">
                    <div class="time" title="${request.time}">
                        <span class="relative-time"></span>
                    </div>
                    <div>
                        <span class="method">${request.method}</span> <span
                                class="uri">${request.path}<#if request.query?has_content>?${request.query}</#if></span>
                        <span class="protocol">HTTP/1.1</span>
                    </div>
                    <#list request.headers as headerName, headerValue>
                        <div class="header">
                            <span class="header-name">${headerName}</span>:<span
                                    class="header-value"> ${headerValue}</span>
                        </div>
                    </#list>
                    <p class="body">
                        ${request.body}
                    </p>

                    <script>
                        document.querySelector('#http-log-${request.id} > .time > .relative-time')
                            .textContent = moment('${request.time}').fromNow()
                    </script>
                </div>
            </#list>
        </div>
    </body>
</html>