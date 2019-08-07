done = function(summary, latency, requests)

    io.write("--------------------------\n")
    local durations = summary.duration / 1000000 -- 执行时间，单位是秒
    local errors = summary.errors.status -- http status不是200，300开头的
    local total = summary.requests -- 总的请求数
    local valid = total - errors -- 有效请求数=总请求数-error请求数


    io.write("Durations:       " .. string.format("%.2f", durations) .. "s" .. "\n")
    io.write("Requests:        " .. summary.requests .. "\n")
    io.write("Avg RT:          " .. string.format("%.2f", latency.mean / 1000) .. "ms" .. "\n")
    io.write("Max RT:          " .. (latency.max / 1000) .. "ms" .. "\n")
    io.write("Min RT:          " .. (latency.min / 1000) .. "ms" .. "\n")
    io.write("Error requests:  " .. errors .. "\n")
    io.write("Valid requests:  " .. valid .. "\n")
    io.write("MAX-QPS/THREAD:  " .. string.format("%.2f", requests.max) .. "\n")
    io.write("AVG-QPS:         " .. string.format("%.2f", valid / durations) .. "\n")
    io.write("--------------------------\n")
end



wrk.method = "POST"
wrk.body = "parameter=%7b%22orderNo%22%3a%2219921023%22%2c%22productCount%22%3a123%2c%22totalAmount%22%3a%224561.35%22%2c%22storeId%22%3a%2212799001%22%2c%22orderDetialList%22%3a%5b%7b%22orderNo%22%3a%2219921023%22%2c%22detailSeq%22%3a1%2c%22Amount%22%3a%224561%22%2c%22remark%22%3a%22%e4%b8%bb%e8%b4%a6%e6%88%b7%22%7d%2c%7b%22orderNo%22%3a%2219921023%22%2c%22detailSeq%22%3a2%2c%22Amount%22%3a%220.35%22%2c%22remark%22%3a%22%e5%88%a9%e6%81%af%22%7d%5d%7d"
wrk.headers["Content-Type"] = "application/x-www-form-urlencoded"