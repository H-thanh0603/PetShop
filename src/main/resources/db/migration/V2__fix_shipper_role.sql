-- Normalize the shipper role spelling; AuthorizationFilter, servlets and
-- JSPs all expect 'shipper'.
UPDATE users SET role = 'shipper' WHERE role = 'shiper';
