local username = KEYS[1]
local timeWindow = tonumber(ARGV[1]) -- 时间窗口，单位：秒
-- 构造 Redis 中存储用户访问次数的键名
local accessKey = "short-link:user-flow-risk-control:" .. username
local currentAccessCount = redis.call("INCR", accessKey)
-- 如果自增的值为 1 说明这个是新增的，那么就设置过期时间
if currentAccessCount == 1 then
    redis.call("SET", accessKey, 1, "EX", timeWindow)
end
return currentAccessCount